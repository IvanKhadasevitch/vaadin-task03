package utils;

import com.vaadin.ui.Notification;
import org.springframework.beans.BeansException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GetBeenFromSpringContext {
    private static final Logger LOGGER = Logger.getLogger(GetBeenFromSpringContext.class.getName());

    public static <T> T getBeen(Class<T> beenClass) {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();

        String beenName = beenClass.getSimpleName();
        if (beenName.length() == 1) {
            beenName = beenName.toLowerCase();
        } else {
            String firstCharOfBeenName = beenName.substring(1,2).toLowerCase();
            beenName = firstCharOfBeenName + beenName.substring(2);
        }

        T been = null;
        try {
            been = context != null
                    ? (T) context.getBean(beenName)
                    : null;
        } catch (BeansException exeption) {
            String message = "In Spring context no been: " + beenClass + "\n";
            LOGGER.log(Level.WARNING, message, exeption.getStackTrace());
            Notification.show(message, Notification.Type.ERROR_MESSAGE);
        }

        return been;
    }
}
