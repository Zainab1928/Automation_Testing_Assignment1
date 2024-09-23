import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicWebTableAutomation {
    WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor js;

    ExtentReports extent;
    ExtentTest test;

    @BeforeClass
    public void setup() {
        // Setup Extent Report using ExtentSparkReporter
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter("target/report.html");
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("Dynamic Web Table Test Report");
        sparkReporter.config().setReportName("Table Sorting Test Cases");
        

        extent = new ExtentReports();
        extent.attachReporter(sparkReporter);

        // Set the path to your ChromeDriver
        System.setProperty("webdriver.chrome.driver", "D:\\Java-Selenium\\Drivers\\chromedriver.exe");

        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;

        // Maximize the browser window and set the timeout
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Navigate to the URL
        driver.get("https://practice.expandtesting.com/tables#edit");
    }

    @Test(priority = 1)
    public void verifyLastNameSorting() {
        test = extent.createTest("verifyLastNameSorting", "Verifies sorting for the Last Name column").assignAuthor("Zainab").assignCategory("functional testcase").assignDevice("Windows");
        verifyColumnSorting("Last Name", "//th//span[@class='last-name']", "//tbody/tr/td[@class='last-name']", false);
    }

    @Test(priority = 2)
    public void verifyFirstNameSorting() {
        test = extent.createTest("verifyFirstNameSorting", "Verifies sorting for the First Name column").assignAuthor("Zainab").assignCategory("functional testcase").assignDevice("Windows");
        verifyColumnSorting("First Name", "//th//span[@class='first-name']", "//tbody/tr/td[@class='first-name']", false);
    }

    @Test(priority = 3)
    public void verifyEmailSorting() {
        test = extent.createTest("verifyEmailSorting", "Verifies sorting for the Email column").assignAuthor("Zainab").assignCategory("functional testcase").assignDevice("Windows");
        verifyColumnSorting("Email", "//th//span[@class='email']", "//tbody/tr/td[@class='email']", false);
    }

    @Test(priority = 4)
    public void verifyDueSorting() {
        test = extent.createTest("verifyDueSorting", "Verifies sorting for the Due column").assignAuthor("Zainab").assignCategory("functional testcase").assignDevice("Windows");
        verifyColumnSorting("Due", "//th//span[@class='dues']", "//tbody/tr/td[@class='dues']", true);
    }

    // Combined method for both string and numeric column sorting
    public void verifyColumnSorting(String columnName, String headerXpath, String cellXpath, boolean isNumeric) {
        System.out.println("Verifying sorting for column: " + columnName);

        // Find the column header element
        WebElement columnHeader = driver.findElement(By.xpath(headerXpath));

        // Get the current values before sorting
        List<String> originalOrder = getColumnValues(cellXpath);

        // Scroll the element into view and click using JavaScript
        js.executeScript("arguments[0].scrollIntoView(true);", columnHeader);
        js.executeScript("arguments[0].click();", columnHeader);

        // Wait until the values have changed (the table gets sorted)
        waitUntilTableUpdates(cellXpath, originalOrder);

        // Fetch the values in the column after sorting in ascending order
        List<String> actualAscOrder = getColumnValues(cellXpath);

        // Validate sorting
        if (isNumeric) {
            List<Double> actualAscOrderNumeric = parseToDoubleList(actualAscOrder);
            List<Double> expectedAscOrder = new ArrayList<>(actualAscOrderNumeric);
            Collections.sort(expectedAscOrder);

            // Assert that the values are sorted in ascending order
            Assert.assertEquals(actualAscOrderNumeric, expectedAscOrder, columnName + " column is NOT sorted correctly in ascending order.");
        } else {
            List<String> expectedAscOrder = new ArrayList<>(actualAscOrder);
            Collections.sort(expectedAscOrder);

            // Assert that the values are sorted in ascending order
            Assert.assertEquals(actualAscOrder, expectedAscOrder, columnName + " column is NOT sorted correctly in ascending order.");
        }

        System.out.println(columnName + " column is sorted correctly in ascending order.");

        // Click the column header again to sort in descending order
        js.executeScript("arguments[0].click();", columnHeader);

        // Wait until the values have changed (the table gets sorted)
        waitUntilTableUpdates(cellXpath, actualAscOrder);

        // Fetch the values in the column after sorting in descending order
        List<String> actualDescOrder = getColumnValues(cellXpath);

        // Validate sorting in descending order
        if (isNumeric) {
            List<Double> actualDescOrderNumeric = parseToDoubleList(actualDescOrder);
            List<Double> expectedDescOrder = new ArrayList<>(actualDescOrderNumeric);
            Collections.sort(expectedDescOrder, Collections.reverseOrder());

            // Assert that the values are sorted in descending order
            Assert.assertEquals(actualDescOrderNumeric, expectedDescOrder, columnName + " column is NOT sorted correctly in descending order.");
        } else {
            List<String> expectedDescOrder = new ArrayList<>(actualAscOrder);
            Collections.reverse(expectedDescOrder);

            // Assert that the values are sorted in descending order
            Assert.assertEquals(actualDescOrder, expectedDescOrder, columnName + " column is NOT sorted correctly in descending order.");
        }

        System.out.println(columnName + " column is sorted correctly in descending order.");
    }

    // Helper method to parse a list of strings to a list of doubles (for numeric sorting)
    public List<Double> parseToDoubleList(List<String> stringList) {
        List<Double> doubleList = new ArrayList<>();
        for (String value : stringList) {
            // Remove any non-numeric characters (like "$") before parsing to double
            doubleList.add(Double.parseDouble(value.replaceAll("[^\\d.]", "")));
        }
        return doubleList;
    }

    // Helper method to get column values
    public List<String> getColumnValues(String cellXpath) {
        List<WebElement> cells = driver.findElements(By.xpath(cellXpath));
        List<String> values = new ArrayList<>();
        for (WebElement cell : cells) {
            values.add(cell.getText());
        }
        return values;
    }

    // Helper method to wait until table updates after sorting
    public void waitUntilTableUpdates(String cellXpath, List<String> originalOrder) {
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                List<String> currentOrder = getColumnValues(cellXpath);
                return !currentOrder.equals(originalOrder);  // Wait until the table values change
            }
        });
    }

    @AfterMethod
    public void captureFailure(ITestResult result) {
        // Check if the test failed
        if (result.getStatus() == ITestResult.FAILURE) {
            try {
                String screenshotPath = takeScreenshot(result.getName());
                test.log(Status.FAIL, "Test Case Failed: " + result.getName());
                test.log(Status.FAIL, "Error: " + result.getThrowable());
                test.addScreenCaptureFromPath(screenshotPath);  // Attach screenshot to the report
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        extent.flush();
    }

    // Helper method to take a screenshot
    public String takeScreenshot(String testName) throws IOException {
        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String destPath = System.getProperty("user.dir") + "/target/images/" + testName + ".png";
        Files.createDirectories(Paths.get(System.getProperty("user.dir") + "/src/images/"));  // Create the folder if it doesn't exist
        Files.copy(srcFile.toPath(), Paths.get(destPath));
        return destPath;
    }

    @AfterClass
    public void teardown() {
        // Close the browser
        if (driver != null) {
            driver.quit();
        }
    }
}
