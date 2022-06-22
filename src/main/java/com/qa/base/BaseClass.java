package com.qa.base;

import com.aventstack.extentreports.Status;
import com.qa.reports.MyExtentReport;
import com.qa.utils.Constants;
import com.qa.utils.TestUtil;
import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.screenrecording.CanRecordScreen;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.annotations.Optional;

import java.io.*;
import java.net.ServerSocket;
import java.net.URL;
import java.util.*;

public class BaseClass {
    protected static ThreadLocal<AppiumDriver> driver = new ThreadLocal<AppiumDriver>();
    protected static ThreadLocal<Properties> props = new ThreadLocal<Properties>();
    protected static ThreadLocal<String> platformName = new ThreadLocal<String>();
    protected static ThreadLocal<String> dateTime = new ThreadLocal<String>();
    protected static ThreadLocal<String> deviceName = new ThreadLocal<String>();
    protected static ThreadLocal<String> platformVersion = new ThreadLocal<String>();
//    protected static ThreadLocal<String> browser = new ThreadLocal<String>();
    protected static ThreadLocal<String> testDataFilePath = new ThreadLocal<String>();
    protected static ThreadLocal<String> stringsFilePath = new ThreadLocal<String>();
    protected static ThreadLocal<String> appConfigFilePath = new ThreadLocal<String>();
    private static AppiumDriverLocalService server;
    private static final long waitTimeOut = 30;
    TestUtil utils = new TestUtil();

    public AppiumDriver getDriver() {
        return driver.get();
    }

    public void setDriver(AppiumDriver driver2) {
        driver.set(driver2);
    }

    public Properties getProps() {
        return props.get();
    }

    public void setProps(Properties props2) {
        props.set(props2);
    }

    public String getPlatformName() {
        return platformName.get();
    }

    public void setPlatformName(String platform2) {
        platformName.set(platform2);
    }
    public String getPlatformVersion() {
        return platformVersion.get();
    }

    public void setPlatformVersion(String platformVersion2) {
        platformVersion.set(platformVersion2);
    }
  /*  public String getBrowserStatus() {
        return browser.get();
    }*/

  /*  public void setBrowserStatus(String browser2) {
        browser.set(browser2);
    }*/
    public String getDateTime() {
        return dateTime.get();
    }

    public void setDateTime(String dateTime2) {
        dateTime.set(dateTime2);
    }

    public String getDeviceName() {
        return deviceName.get();
    }

    public void setDeviceName(String deviceName2) {
        deviceName.set(deviceName2);
    }

    public String getTestDataFilePath() {
        return testDataFilePath.get();
    }

    public void setTestDataFilePath(String testDataFilePath2) {
        testDataFilePath.set(testDataFilePath2);
    }

    public String getStringsFileFilePath() {
        return stringsFilePath.get();
    }

    public void setStringsFilePath(String stringsFilePath2) {
        stringsFilePath.set(stringsFilePath2);
    }

    public String getAppConfigFilePath() {
        return appConfigFilePath.get();
    }

    public void setAppConfigFilePath(String stringsFilePath2) {
        appConfigFilePath.set(stringsFilePath2);
    }


    public BaseClass() {
        PageFactory.initElements(new AppiumFieldDecorator(getDriver()), this);
    }


    @Parameters({"emulator", "platformVersion","platformName", "udid", "deviceName", "systemPort",
            "chromeDriverPort", "wdaLocalPort", "webkitDebugProxyPort"})
    @BeforeTest
    public void launchApp(@Optional("androidOnly") String emulator,@Optional("androidOnly") String platformVersion, String platformName, String udid, String deviceName,
                          @Optional("androidOnly") String systemPort, @Optional("androidOnly") String chromeDriverPort,
                          @Optional("iOSOnly") String wdaLocalPort, @Optional("iOSOnly") String webkitDebugProxyPort) throws IOException {

        String propFilePath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "main" + File.separator + "resources" +
                File.separator + "appLaunch.properties";

        String testDataPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources" + File.separator + "testdata" +
                File.separator + "Login.json";

        String stringsPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources" + File.separator + "testdata" +
                File.separator + "Strings.json";

        String appPath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "test" + File.separator + "resources" + File.separator + "app" +
                File.separator;
        InputStream inputStream = new FileInputStream(propFilePath);
        Properties props = new Properties();
        props.load(inputStream);

        String strFile = "logs" + File.separator + platformName + "_" + deviceName;
        File logFile = new File(strFile);
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        ThreadContext.put("ROUTINGKEY", strFile);
        setDateTime(utils.getDateTime());
        setPlatformName(platformName);
        setDeviceName(deviceName);
        setPlatformVersion(platformVersion);
       // setBrowserStatus(browser);
        setTestDataFilePath(testDataPath);
        setStringsFilePath(stringsPath);
        setAppConfigFilePath(propFilePath);
        setProps(props);

        AppiumDriver driver = null;
        URL url;
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability(MobileCapabilityType.PLATFORM_NAME, platformName);
        caps.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
        caps.setCapability(MobileCapabilityType.UDID, udid);

        url = new URL(props.getProperty("appiumURL"));
        try {
            if (platformName.equalsIgnoreCase("Android")) {
                if (emulator.equalsIgnoreCase("true")) {
                    caps.setCapability("avd", deviceName);
                    caps.setCapability("avdLaunchTimeout", 120000);
                }
                caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, props.getProperty("androidAutomationName"));
              caps.setCapability(MobileCapabilityType.PLATFORM_VERSION, platformVersion);
                caps.setCapability("ignoreHiddenApiPolicyError", true);
                caps.setCapability(MobileCapabilityType.BROWSER_NAME, "Chrome");
                String path= System.getProperty("user.dir") + File.separator+"src"+ File.separator+"test"+ File.separator+"resources"+ File.separator+"browserdrivers";
                caps.setCapability("chromedriverExecutableDir", path);
                //  caps.setCapability("systemPort", systemPort);
                //caps.setCapability("chromeDriverPort", chromeDriverPort);
                driver = new AndroidDriver(url, caps);
            } else if (platformName.equalsIgnoreCase("iOS")) {
                caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, props.getProperty("iosAutomationName"));
                caps.setCapability("bundleId", props.getProperty("iosBundleId"));
                caps.setCapability("wdaLocalPort", wdaLocalPort);
                caps.setCapability("webkitDebugProxyPort", webkitDebugProxyPort);
                // caps.setCapability(MobileCapabilityType.APP, appPath + props.getProperty("iosAppName"));
                driver = new IOSDriver(url, caps);
            }
            //  utils.log().info(getPlatformName() + " driver is Initialized !!");
        } catch (Exception e) {
            utils.log().error(getPlatformName() + " driver initialization Failed");
            e.printStackTrace();
        }
        setDriver(driver);
        System.out.println("driver initialized:");
        utils.log().info("driver initialized: ");
        getDriver().get("https://naveenautomationlabs.com/opencart");
    }

    @BeforeSuite
    public void beforeSuite() throws Exception, Exception {
        ThreadContext.put("ROUTINGKEY", "ServerLogs");
       // System.out.println(System.getProperty("os.name"));
        if (System.getProperty("os.name").contains(String.valueOf(Constants.Os.Windows))) {
        server = getAppiumServerDefault();
        } else if (System.getProperty("os.name").contains(String.valueOf(Constants.Os.Mac))) {
         server = getAppiumService();
        }
        if (!checkIfAppiumServerIsRunnning(4723)) { // May not work exactly appium limitation
            server.start();
            server.clearOutPutStreams(); // -> Comment this if you don't want to see server logs in the console
            utils.log().info("Appium server started");
        } else {
            utils.log().info("Appium server already running");
        }
    }

    public boolean checkIfAppiumServerIsRunnning(int port) throws Exception {
        boolean isAppiumServerRunning = false;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            socket.close();
        } catch (IOException e) {
           // System.out.println("1");
            isAppiumServerRunning = true;
        } finally {
            socket = null;
        }
        return isAppiumServerRunning;
    }

    // for Windows
    public AppiumDriverLocalService getAppiumServerDefault() throws IOException {
        String propFilePath = System.getProperty("user.dir") + File.separator + "src" +
                File.separator + "main" + File.separator + "resources" +
                File.separator + "appLaunch.properties";
        if ((new TestUtil().getKeyValue(propFilePath,"browser")).equalsIgnoreCase("yes")){
            System.out.println("In browser if condition");
           // return  AppiumDriverLocalService.buildService(new AppiumServiceBuilder().usingAnyFreePort().withArgument(() -> "--allow-insecure","chromedriver_autodownload"));
            return AppiumDriverLocalService.buildDefaultService(); // Above is the expected it's not working as expected so using default option as temp fix
        }else
            return AppiumDriverLocalService.buildDefaultService();

    }

    // for mac
    public AppiumDriverLocalService getAppiumService() throws IOException {
        HashMap<String, String> environment = new HashMap<String, String>();
        environment.put("PATH", "/Users/navakanthnavi/Library/Android/sdk:/Users/navakanthnavi/Library/Android/sdk/platform-tools:/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin:/Library/Apple/usr/bin" + System.getenv("PATH"));
        environment.put("ANDROID_HOME", "/Users/navakanthnavi/Library/Android/sdk");

        if ((utils.getKeyValue(getAppConfigFilePath(),"browser")).equalsIgnoreCase("true")){
            System.out.println("In browser if condition");
            return AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                    .usingDriverExecutable(new File("/usr/local/bin/node"))
                    .withAppiumJS(new File("/usr/local/lib/node_modules/appium/build/lib/main.js"))
                    .usingPort(4723)
                    .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                    .withArgument(() -> "--allow-insecure","chromedriver_autodownload")
                    .withEnvironment(environment)
                    .withLogFile(new File("ServerLogs/server.log")));
        }
        else{
            return AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                    .usingDriverExecutable(new File("/usr/local/bin/node"))
                    .withAppiumJS(new File("/usr/local/lib/node_modules/appium/build/lib/main.js"))
                    .usingPort(4723)
                    .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
//                    .withArgument(() -> "--allow-insecure","chromedriver_autodownload")
                    .withEnvironment(environment)
                    .withLogFile(new File("ServerLogs/server.log")));
        }
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        if (server.isRunning()) {
            server.stop();
            utils.log().info("Appium server stopped");
        }
    }

    @AfterTest
    public void tearDown() {
        getDriver().quit();
    }

    @BeforeMethod
    public void beforeMethod(ITestResult result) throws IOException {
        if (utils.getKeyValue(getAppConfigFilePath(), "screenRecording").equalsIgnoreCase("true"))
            ((CanRecordScreen) getDriver()).startRecordingScreen();
    }

    @AfterMethod
    public void redirect(ITestResult result) throws IOException {
        if (utils.getKeyValue(getAppConfigFilePath(), "screenRecording").equalsIgnoreCase("true"))
            stopVideoRecording(result);
        if (result.getStatus() == ITestResult.FAILURE) {

        } else if (result.getStatus() == ITestResult.SUCCESS) {

        }
        closeApp();
        launchAppAgain();
    }

    public void addLogs(String message) {
        utils.log().info(message);
        MyExtentReport.getTest().log(Status.INFO, message);
    }

    public void addLogs(String message, String str) {
        utils.log().info(message);
    }

    public void closeApp() {
        ((InteractsWithApps) getDriver()).closeApp();
        addLogs("App is Closed !!");

    }

    public void launchAppAgain() {
        ((InteractsWithApps) getDriver()).launchApp();
        addLogs("App is Launched !!");
    }

    public void waitForEleVisibility(MobileElement ele) {

        WebDriverWait wait = new WebDriverWait(getDriver(), waitTimeOut);
        wait.until(ExpectedConditions.visibilityOf(ele));
    }

    public void waitForEleVisibility(MobileElement ele, Long time) {
        WebDriverWait wait = new WebDriverWait(getDriver(), time);
        wait.until(ExpectedConditions.visibilityOf(ele));
    }

    public void waitForEleClickable(MobileElement ele, Long time) {
        WebDriverWait wait = new WebDriverWait(getDriver(), time);
        wait.until(ExpectedConditions.elementToBeClickable(ele));
    }

    public void tapOnElement(MobileElement ele) {
        waitForEleVisibility(ele);
        ele.click();
        addLogs("Tapped on element " + ele);
    }

    public void clearElement(MobileElement ele) {
        waitForEleVisibility(ele);
        ele.clear();
    }

    public void sendDataToElement(MobileElement ele, String value) {
        waitForEleVisibility(ele);
        clearElement(ele);
        ele.sendKeys(value);
        addLogs("Entered data in " + ele + " as " + value);
    }

    public String getAndroidElementAttribute(MobileElement ele, String attributeName) {
        waitForEleVisibility(ele);
        return ele.getAttribute(attributeName);
    }

    public String getIOSElementAttribute(MobileElement ele, String attributeName) {
        waitForEleVisibility(ele);
        return ele.getAttribute(attributeName);
    }

    // in iOS text attribute will not work hence using label attribute
    public String getText(MobileElement ele) {
        String text = null;
        if (getPlatformName().equalsIgnoreCase("Android")) {
            text = getAndroidElementAttribute(ele, "text");
        } else if (getPlatformName().equalsIgnoreCase("iOS")) {
            text = getIOSElementAttribute(ele, "label");
        }
        return text;
    }


    // In below examples test-Inventory item page - Scrollable parent element () &
    // test-Price is the child element or target ele
    // scrollToElement_Android1 - Can be used if app has multiple scrolls for diff pages
    // else use scrollToElement_Android2 if app has only one scroll for diff pages
    // In android scroll tag is "android.widget.ScrollView" which means ele can be scrollable

    public MobileElement scrollToElement_Android1() {
        return (MobileElement) ((FindsByAndroidUIAutomator) getDriver()).findElementByAndroidUIAutomator(
                "new UiScrollable(new UiSelector()" + ".description(\"test-Item\")).scrollable(true)).scrollIntoView("
                        + "new UiSelector().description(\"test-FINISH\"));");
    } // CHECKOUT: OVERVIEW

    public MobileElement scrollToElement_Android2(String value) {
        return (MobileElement) ((FindsByAndroidUIAutomator) getDriver()).findElementByAndroidUIAutomator(
                "new UiScrollable(new UiSelector()" + ".scrollable(true)).scrollIntoView("
                        + "new UiSelector().description(" + value + "));");
    }

    public void scrollTillText(String text){
        ((AndroidDriver) getDriver()).
                findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true).instance(0))." +
                        "scrollIntoView(new UiSelector().text(\""+text+"\"))");
    }


    // Scrollable element is identified by XCUIElementTypeScrollView tag
    public void iosScroll(String scrollType) {
        RemoteWebElement parentElement = (RemoteWebElement) getDriver().findElement(By.className("XCUIElementTypeScrollView"));
        String parElementID = parentElement.getId();
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        scrollObject.put("element", parElementID);
        scrollObject.put("direction", scrollType.toLowerCase(Locale.ROOT));
        getDriver().executeScript("mobile:scroll", scrollObject);
    }
    // Scroll to particular element using below
    // identificationType - class or id
    // attributeType - label or name
    // attributeValue - label value or name value
    // Scroll to particular element using predicateString

    public void iosScroll(String identificationType, String attributeType, String attributeValue) {
        HashMap<String, String> scrollObject = new HashMap<String, String>();
        switch (identificationType) {
            case "class":
                RemoteWebElement parentElement = (RemoteWebElement) getDriver().findElement(By.className("XCUIElementTypeScrollView")); // Parent tag
                String parElementID = parentElement.getId();
                scrollObject.put("element", parElementID);
                if (attributeType.equalsIgnoreCase("label")) {
                    scrollObject.put("predicateString", "label == '" + attributeValue + "'");
                } else if (attributeType.equalsIgnoreCase("name")) {
                    scrollObject.put("name", attributeValue);
                }
                getDriver().executeScript("mobile:scroll", scrollObject);
                break;
            case "id":
                RemoteWebElement actualElement = (RemoteWebElement) getDriver().findElement(By.name(attributeValue));
                String elementID = actualElement.getId();
                scrollObject.put("element", elementID);
                scrollObject.put("toVisible", "RandomText");
                getDriver().executeScript("mobile:scroll", scrollObject);
                break;
        }
    }

    public void keyboardActions(String key) {
        getDriver().findElement(MobileBy.AccessibilityId(key)).click();
    }

    public synchronized void stopVideoRecording(ITestResult result) throws IOException {
        String media = ((CanRecordScreen) getDriver()).stopRecordingScreen();

        Map<String, String> params = result.getTestContext().getCurrentXmlTest().getAllParameters();
        String dirPath = "videos" + File.separator + params.get("platformName") + "_" + params.get("deviceName")
                + File.separator + getDateTime() + File.separator + result.getTestClass().getRealClass().getSimpleName();

        File videoDir = new File(dirPath);

        synchronized (videoDir) {
            if (!videoDir.exists()) {
                videoDir.mkdirs();
            }
        }

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(videoDir + File.separator + result.getName() + ".mp4");
            stream.write(Base64.decodeBase64(media));
            stream.close();
            //  utils.log().info("video path: " + videoDir + File.separator + result.getName() + ".mp4");
        } catch (Exception e) {
            //  utils.log().error("error during video capture" + e.toString());
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void switchToNativeContext(){
        getDriver().context("NATIVE_APP");
    }
    public void switchToContextIndex(int num){
        Set<String> contextHandles = getDriver().getContextHandles();
        for(Object contextHandle: contextHandles){
            System.out.println(contextHandle.toString());
        }
        getDriver().context(contextHandles.toArray()[num].toString());
    }
    public void switchToContextName(String contextName){
        getDriver().context(contextName);
    }
}