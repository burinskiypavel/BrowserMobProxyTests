package Tests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import java.util.List;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;

public class Test2 {

    public static WebDriver driver;
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

        try{
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // получить данные HAR
        Har har = proxy.getHar();
            System.out.println("HAR: " + har.getLog().getVersion());
            for(int i = 0; i < har.getLog().getEntries().size(); i++){
                String link = har.getLog().getEntries().get(i).getRequest().getUrl();
                System.out.println("HAR LINK: " + link);
            }

            List<WebElement> elements = driver.findElements(By.className("q"));
            Assert.assertNotEquals(0 , elements.size());
        }

        @AfterClass
        public static void tearDown()
    {
        driver.close();
        driver.quit();
        proxy.stop();
    }

}
