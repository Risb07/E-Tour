package com.etour.notification.transport;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.etour.notification.domain.Notification;
import com.etour.notification.domain.NotificationAttachment;

import jakarta.mail.internet.MimeMessage;

/** Real delivery over SMTP, using the same mail credentials the eTour backends use. */
public class SmtpNotificationTransport implements NotificationTransport {

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpNotificationTransport(JavaMailSender mailSender, String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public String name() {
        return "smtp";
    }

    @Override
    public void send(Notification notification) throws Exception {
        var attachments = notification.getAttachments();
        boolean multipart = attachments != null && !attachments.isEmpty();

        MimeMessage message = mailSender.createMimeMessage();
        // The boolean is what decides multipart vs simple. Passing true
        // unconditionally would work, but it wraps every plain-text message in a
        // multipart envelope for no reason, so it tracks whether there is
        // actually anything to attach.
        MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "UTF-8");

        helper.setFrom(from);
        helper.setTo(notification.getRecipient());
        helper.setSubject(notification.getSubject());
        helper.setText(notification.getBody());

        if (multipart) {
            for (NotificationAttachment attachment : attachments) {
                helper.addAttachment(
                        attachment.getFilename(),
                        new ByteArrayResource(attachment.getContent()),
                        attachment.getContentType());
            }
        }

        // Any MailException propagates: the worker turns it into lastError and
        // decides whether the one retry is still available.
        mailSender.send(message);
    }
}
