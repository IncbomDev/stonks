package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.share.Share;
import org.bukkit.Bukkit;

import java.time.LocalDateTime;

public class DividendManager {

    /**
     * The shortest dividends period is one day. This time out
     * is half a day because we can't be sure the runnable will be
     * executed at the very exact time.
     * <p>
     * This is arbitrarily set at 1 hour which is much longer than
     * the dividends runnable period.
     */
    private static final long DIVIDEND_SAFE_TIME_OUT = 1000 * 60 * 60;

    public DividendManager() {
        //:: ->reference to a method instead of having to use lambda expression
        // Check for dividends every hour
        Bukkit.getScheduler().runTaskTimer(Stonks.plugin, this::checkForDividends, 0, 20 * 60 * 10);
    }

    public void checkForDividends() {

        // Check if it's the right schedule
        if (LocalDateTime.now().getHour() != Stonks.plugin.configManager.dividendsRedeemHour)
            return;

        // Loop through stocks with dividends support
        for (Stock stock : Stonks.plugin.stockManager.getStocks())
            if (stock.hasDividends() && stock.getDividends().canGiveDividends()) {

                // Apply dividends cooldown
                stock.getDividends().setLastApplication(System.currentTimeMillis() - DIVIDEND_SAFE_TIME_OUT);

                // Give money to shares
                for (Share share : Stonks.plugin.shareManager.getByStock(stock))
                    share.addToWallet(stock.getDividends().applyFormula(share));
            }
    }
}
