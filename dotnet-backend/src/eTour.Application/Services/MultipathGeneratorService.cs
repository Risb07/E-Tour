using eTour.Application.Common;
using eTour.Application.Dtos;
using eTour.Domain.Entities;
using eTour.Domain.Enums;
using Microsoft.Extensions.Logging;

namespace eTour.Application.Services;

/// <summary>
/// Puts tours onto multiple navigation paths automatically, driven by TourTagRule rows. Only ever
/// creates RELATIONSHIPS (tour_category links, optionally a TourProduct row) - never copies a
/// tour, because duplicated tours drift apart in price/availability the moment one is edited.
/// Preview/Apply share one Run(commit) implementation so the dry run can never disagree with
/// what actually happens.
/// </summary>
public class MultipathGeneratorService : IMultipathGeneratorService
{
    private readonly IGenericRepository<TourTagRule, long> _ruleRepository;
    private readonly IGenericRepository<Tour, long> _tourRepository;
    private readonly IGenericRepository<TourProduct, long> _productRepository;
    private readonly IMultipathLinkRepository _linkRepository;
    private readonly ILogger<MultipathGeneratorService> _logger;

    public MultipathGeneratorService(IGenericRepository<TourTagRule, long> ruleRepository, IGenericRepository<Tour, long> tourRepository,
        IGenericRepository<TourProduct, long> productRepository, IMultipathLinkRepository linkRepository,
        ILogger<MultipathGeneratorService> logger)
    {
        _ruleRepository = ruleRepository;
        _tourRepository = tourRepository;
        _productRepository = productRepository;
        _linkRepository = linkRepository;
        _logger = logger;
    }

    public Task<MultipathPreviewResponse> PreviewAsync(CancellationToken ct = default) => Run(commit: false, ct);

    public Task<MultipathPreviewResponse> ApplyAsync(CancellationToken ct = default) => Run(commit: true, ct);

    /// <param name="commit">false = dry run (nothing written), true = persist changes.</param>
    private async Task<MultipathPreviewResponse> Run(bool commit, CancellationToken ct)
    {
        var rules = _ruleRepository.Query().Where(r => r.Active).OrderBy(r => r.Priority).ToList();

        // Only ACTIVE tours are placed on public paths.
        var tours = _tourRepository.Query().Where(t => t.Status == TourStatus.ACTIVE).ToList();

        var changes = new List<PlannedChange>();
        var rulesWithNoMatches = new List<string>();
        var newCategoryLinks = 0;
        var newProducts = 0;

        foreach (var rule in rules)
        {
            var matched = 0;
            var categoryName = _tourRepository.Query()
                .SelectMany(t => t.Categories).Where(c => c.CategoryId == rule.TargetCategoryId)
                .Select(c => c.CategoryName).FirstOrDefault() ?? "";
            var subSectorName = rule.TargetSubSectorId is { } subSectorId
                ? _productRepository.Query().Where(p => p.SubSectorId == subSectorId).Select(p => p.SubSector.Name).FirstOrDefault() ?? ""
                : null;

            foreach (var tour in tours)
            {
                if (!Matches(rule, tour))
                {
                    continue;
                }
                matched++;

                // ---- Path 1: category link ----
                var alreadyInCategory = _tourRepository.Query()
                    .Where(t => t.TourId == tour.TourId)
                    .SelectMany(t => t.Categories)
                    .Any(c => c.CategoryId == rule.TargetCategoryId);

                changes.Add(new PlannedChange(tour.TourId, tour.Title, rule.Name, "CATEGORY", categoryName, alreadyInCategory));

                if (!alreadyInCategory)
                {
                    newCategoryLinks++;
                    if (commit)
                    {
                        await _linkRepository.LinkTourToCategoryAsync(tour.TourId, rule.TargetCategoryId, ct);
                    }
                }

                // ---- Path 2: sector/product entry (opt-in per rule) ----
                if (rule.TargetSubSectorId is { } targetSubSectorId)
                {
                    var productExists = _productRepository.Query()
                        .Any(p => p.SubSectorId == targetSubSectorId && p.TourId == tour.TourId);

                    changes.Add(new PlannedChange(tour.TourId, tour.Title, rule.Name, "PRODUCT", subSectorName ?? "", productExists));

                    if (!productExists)
                    {
                        newProducts++;
                        if (commit)
                        {
                            await _productRepository.AddAsync(BuildProduct(tour, targetSubSectorId), ct);
                        }
                    }
                }
            }

            if (matched == 0)
            {
                rulesWithNoMatches.Add(rule.Name);
            }
        }

        if (commit)
        {
            _logger.LogInformation("Multipath generator applied: {NewLinks} new category links, {NewProducts} new products, {RuleCount} rules",
                newCategoryLinks, newProducts, rules.Count);
        }

        return new MultipathPreviewResponse
        {
            RulesEvaluated = rules.Count,
            ToursEvaluated = tours.Count,
            NewCategoryLinks = newCategoryLinks,
            NewProducts = newProducts,
            Changes = changes,
            RulesWithNoMatches = rulesWithNoMatches
        };
    }

    /// <summary>Mirrors the tour's own details so the generated product isn't a blank record.</summary>
    private static TourProduct BuildProduct(Tour tour, long subSectorId) => new()
    {
        SubSectorId = subSectorId,
        TourId = tour.TourId,
        Name = tour.Title,
        Description = tour.Description,
        BaseCost = tour.BasePrice,
        DurationDays = tour.DurationDays,
        // Nights is conventionally one less than days for a return trip.
        DurationNights = Math.Max(0, tour.DurationDays - 1),
        TourCode = tour.TourCode.ToString(),
        Active = true,
        SortOrder = 0
    };

    /// <summary>Evaluates one rule against one tour.</summary>
    private static bool Matches(TourTagRule rule, Tour tour)
    {
        if (rule.MatchField == RuleMatchField.ALL || rule.MatchOperator == RuleOperator.ANY)
        {
            return true;
        }

        return rule.MatchField switch
        {
            RuleMatchField.TOUR_CODE => CompareText(tour.TourCode.ToString(), rule.MatchOperator, rule.MatchValue),
            RuleMatchField.TITLE => CompareText(tour.Title ?? "", rule.MatchOperator, rule.MatchValue),
            RuleMatchField.BASE_PRICE => CompareNumber(tour.BasePrice, rule.MatchOperator, Parse(rule.MatchValue), Parse(rule.MatchValueTo)),
            RuleMatchField.DURATION_DAYS => CompareNumber(tour.DurationDays, rule.MatchOperator, Parse(rule.MatchValue), Parse(rule.MatchValueTo)),
            _ => false
        };
    }

    private static bool CompareText(string actual, RuleOperator op, string? expected)
    {
        if (expected is null)
        {
            return false;
        }
        return op switch
        {
            RuleOperator.EQUALS => string.Equals(actual, expected.Trim(), StringComparison.OrdinalIgnoreCase),
            RuleOperator.CONTAINS => actual.Contains(expected.Trim(), StringComparison.OrdinalIgnoreCase),
            // Numeric operators are meaningless on text - treat as no match rather than throwing.
            _ => false
        };
    }

    private static bool CompareNumber(decimal? actual, RuleOperator op, decimal? from, decimal? to)
    {
        if (actual is null || from is null)
        {
            return false;
        }
        return op switch
        {
            RuleOperator.EQUALS => actual == from,
            RuleOperator.GREATER_THAN => actual > from,
            RuleOperator.LESS_THAN => actual < from,
            RuleOperator.BETWEEN => to is not null && actual >= from && actual <= to,
            _ => false
        };
    }

    /// <summary>Null-safe, exception-safe numeric parse - a bad value simply never matches.</summary>
    private static decimal? Parse(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }
        return decimal.TryParse(value.Trim(), out var parsed) ? parsed : null;
    }
}
