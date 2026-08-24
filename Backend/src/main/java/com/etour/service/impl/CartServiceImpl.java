package com.etour.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.dto.BookingRequest;
import com.etour.dto.BookingResponse;
import com.etour.dto.CartAddonResponse;
import com.etour.dto.CartRequest;
import com.etour.dto.CartResponse;
import com.etour.entity.Cart;
import com.etour.entity.CartAddon;
import com.etour.entity.Customer;
import com.etour.entity.TourAddon;
import com.etour.entity.TourSchedule;
import com.etour.enums.CartStatus;
import com.etour.exception.IllegalOperationException;
import com.etour.exception.ResourceNotFoundException;
import com.etour.repository.CartAddonRepository;
import com.etour.repository.CartRepository;
import com.etour.repository.TourAddonRepository;
import com.etour.repository.TourScheduleRepository;
import com.etour.security.CurrentUserProvider;
import com.etour.service.BookingService;
import com.etour.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartAddonRepository cartAddonRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourAddonRepository tourAddonRepository;
    private final CurrentUserProvider currentUserProvider;
    private final BookingService bookingService;

    public CartServiceImpl(CartRepository cartRepository, CartAddonRepository cartAddonRepository,
            TourScheduleRepository tourScheduleRepository, TourAddonRepository tourAddonRepository,
            CurrentUserProvider currentUserProvider, BookingService bookingService) {
        this.cartRepository = cartRepository;
        this.cartAddonRepository = cartAddonRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.tourAddonRepository = tourAddonRepository;
        this.currentUserProvider = currentUserProvider;
        this.bookingService = bookingService;
    }

    @Override
    @Transactional
    public CartResponse addToCart(CartRequest request) {
        Customer customer = currentUserProvider.currentCustomer();

        TourSchedule schedule = tourScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Tour schedule not found"));

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setSchedule(schedule);
        cart.setPaxSummary(request.getNumberOfPassengers() + " passenger(s)");
        cart.setStatus(CartStatus.ACTIVE);
        // Keep the composition alongside the headcount so checkout doesn't have
        // to re-derive it. The at-least-one-adult rule is enforced on the
        // booking (see BookingServiceImpl#validateComposition), not here -
        // adding to a cart commits to nothing.
        cart.setAdultCount(request.getAdultCount());
        cart.setChildCount(request.getChildCount());

        BigDecimal estimated = schedule.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfPassengers()));

        Cart savedCart = cartRepository.save(cart);

        List<CartAddon> addonRows = new ArrayList<>();
        for (CartRequest.AddonSelection sel : request.getAddons()) {
            // Scoped to this schedule's tour and to active add-ons only - the
            // same rule the booking path applies, so a cart can't carry an
            // add-on that checkout would reject.
            TourAddon catalog = tourAddonRepository
                    .findByAddonIdAndTour_TourIdAndStatusTrue(sel.getAddonId(), schedule.getTour().getTourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Add-on not found: " + sel.getAddonId()));
            CartAddon addonRow = new CartAddon();
            addonRow.setCart(savedCart);
            addonRow.setAddon(catalog);
            addonRow.setQuantity(sel.getQuantity());
            BigDecimal lineTotal = catalog.getPrice().multiply(BigDecimal.valueOf(sel.getQuantity()));
            addonRow.setEstimatedCost(lineTotal);
            estimated = estimated.add(lineTotal);
            addonRows.add(addonRow);
        }
        cartAddonRepository.saveAll(addonRows);

        savedCart.setEstimatedAmount(estimated);
        savedCart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(savedCart);

        return toResponse(savedCart, addonRows);
    }

    @Override
    public List<CartResponse> getMyCart() {
        Customer customer = currentUserProvider.currentCustomer();
        return cartRepository.findByCustomer_CustomerIdAndStatus(customer.getCustomerId(), CartStatus.ACTIVE).stream()
                .map(c -> toResponse(c, cartAddonRepository.findByCart_CartId(c.getCartId())))
                .toList();
    }

    @Override
    @Transactional
    public void removeFromCart(Long cartId) {
        Customer customer = currentUserProvider.currentCustomer();
        Cart cart = cartRepository.findByCartIdAndCustomer_CustomerId(cartId, customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        cartAddonRepository.deleteByCart_CartId(cartId);
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public BookingResponse checkout(Long cartId) {
        Customer customer = currentUserProvider.currentCustomer();
        Cart cart = cartRepository.findByCartIdAndCustomer_CustomerId(cartId, customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalOperationException("This cart item is no longer active");
        }

        List<CartAddon> cartAddons = cartAddonRepository.findByCart_CartId(cartId);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setScheduleId(cart.getSchedule().getScheduleId());
        // pax_summary was stored as "<n> passenger(s)" - recover the number.
        int passengers = extractPassengerCount(cart.getPaxSummary());
        bookingRequest.setNumberOfPassengers(passengers);
        // Carry the composition through so the booking knows how many adult
        // and child forms the passenger step should render. Older cart rows
        // have neither, and the booking falls back to deriving the mix from
        // the passengers' dates of birth.
        bookingRequest.setAdultCount(cart.getAdultCount());
        bookingRequest.setChildCount(cart.getChildCount());

        List<BookingRequest.BookingAddonSelection> selections = new ArrayList<>();
        for (CartAddon ca : cartAddons) {
            BookingRequest.BookingAddonSelection sel = new BookingRequest.BookingAddonSelection();
            sel.setAddonId(ca.getAddon().getAddonId());
            sel.setQuantity(ca.getQuantity());
            selections.add(sel);
        }
        bookingRequest.setAddons(selections);

        BookingResponse booking = bookingService.createBooking(bookingRequest);

        cart.setStatus(CartStatus.CONVERTED_TO_BOOKING);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return booking;
    }

    private int extractPassengerCount(String paxSummary) {
        if (paxSummary == null) return 1;
        try {
            return Integer.parseInt(paxSummary.trim().split(" ")[0]);
        } catch (Exception e) {
            return 1;
        }
    }

    private CartResponse toResponse(Cart cart, List<CartAddon> addons) {
        List<CartAddonResponse> addonDtos = addons.stream()
                .map(a -> new CartAddonResponse(a.getCartAddonId(), a.getAddon().getAddonId(),
                        a.getAddon().getAddonName(), a.getQuantity(), a.getEstimatedCost()))
                .toList();

        CartResponse response = new CartResponse(
                cart.getCartId(),
                cart.getSchedule().getScheduleId(),
                cart.getSchedule().getTour().getTourId(),
                cart.getSchedule().getTour().getTitle(),
                cart.getPaxSummary(),
                cart.getEstimatedAmount(),
                cart.getStatus().name(),
                addonDtos);
        response.setAdultCount(cart.getAdultCount());
        response.setChildCount(cart.getChildCount());
        return response;
    }
}
