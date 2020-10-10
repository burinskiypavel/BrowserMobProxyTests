package Tests;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class Test1 {

    public static WebDriver driver;
    WebDriverWait wait;
    public static BrowserMobProxy proxy;

    @BeforeClass
    public static void setup(){
        String path = System.getProperty("user.dir") + "/webdriver/chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", path);

        //старт прокси
        proxy = new BrowserMobProxyServer();
        proxy.setTrustAllServers(true);
        proxy.start(9091);

        //получить обьект Selenium
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        //настройка для драйвера
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--ignore-certificate-errors", "--user-data-dir=somedirectory");

        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        //создание драйвера
        driver = new ChromeDriver(capabilities);

        //включить более детальный захват HAR
        proxy.newHar("www.google.com");
    }

        @Test
        public void testSearch(){

        driver.get("https://www.google.com/");

        WebElement search = driver.findElement(By.name("q"));
        search.sendKeys("GeForce 1650");
        search.sendKeys(Keys.ENTER);

            wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3.LC20lb")));

        // получить данные HAR
        Har har = proxy.getHar();
            for (HarEntry entry : har.getLog().getEntries()) {
                HarRequest request = entry.getRequest();
                HarResponse response = entry.getResponse();

                if(response.getStatus() == 500){
                    Assert.fail(request.getUrl() + " returns 500 error");
                }

                System.out.println(response.getStatus() + " : " + request.getUrl()
                        + ", " + entry.getTime() + "ms");

                assertThat(response.getStatus(), is(200));
            }
        }

        @AfterClass
        public static void tearDown()
    {
        driver.close();
        driver.quit();
        proxy.stop();
    }

}
