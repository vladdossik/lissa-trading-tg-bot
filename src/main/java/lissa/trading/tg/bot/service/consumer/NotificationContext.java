package lissa.trading.tg.bot.service.consumer;

import org.springframework.stereotype.Component;

@Component
public class NotificationContext {
    private static final ThreadLocal<Boolean> externalSourceContext = ThreadLocal.withInitial(() -> false);

    public void setFromExternalSource(boolean isExternal) {
        externalSourceContext.set(isExternal);
    }

    public boolean isExternalSource() {
        return externalSourceContext.get();
    }

    public void clear() {
        externalSourceContext.set(false);
    }
}