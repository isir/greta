package furhatos.app.gretamimic.activemq

import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File

class AudioUrlSelenium() {

    private lateinit var audioFilePath: String
    private lateinit var driver_path: String
    private lateinit var driver: WebDriver

    fun getAudioFilePath(): String {
        return audioFilePath
    }

    fun getDriver():WebDriver{
        return driver
    }

    fun setDriver(headless:Boolean){

        // Set the path to your WebDriver executable (EdgeDriver in this case)
        System.setProperty("webdriver.edge.driver", driver_path)

        // Set Edge options for headless mode
        val options = EdgeOptions()
        options.setPageLoadStrategy(PageLoadStrategy.EAGER.toString())

        if (headless){

            options.setCapability("ms:edgeOptions", mapOf(
                "args" to listOf("--headless", "--disable-gpu", "--no-sandbox")
            ))
        }

        // Initialize the WebDriver
        driver = EdgeDriver(options)
    }

    fun setAudioFilePath(filePath: String) {
        audioFilePath = filePath
    }

    fun setDriverPath(path:String){
        driver_path = path
    }

    fun removeDriver(){
        driver.quit()
    }

    fun getDriverPath():String{
        return driver_path
    }

    fun goToAudioUploadPage(){

        // Navigate to the lip sync tool
        driver.get("https://furhat.io/audio/create")

        // Find the email input field and enter the email
        val emailField: WebElement = driver.findElement(By.name("email"))
        emailField.sendKeys("aquilaeduciel@gmail.com")

        // Find the password input field and enter the password
        val passwordField: WebElement = driver.findElement(By.name("password"))
        passwordField.sendKeys("Aquilae")

        // Find the login button and click it
        val loginButton: WebElement = driver.findElement(By.cssSelector("input.btn-submit"))
        loginButton.click()

    }
    fun retrieveOnlineUrl(): String? {
        var lastAlignedFileUrl = ""
        try {
            // Wait until the file upload input is present
            val fileInput: WebElement = WebDriverWait(driver, 10)
                .until(ExpectedConditions.presenceOfElementLocated(By.name("files[]")))

            // Use absolute path for file input
            fileInput.sendKeys(File(audioFilePath).absolutePath)

            // Find and submit the form
            val uploadButton: WebElement = driver.findElement(By.cssSelector("input.btn-submit.bg-clr-green"))
            uploadButton.click()

            driver.get("https://furhat.io/audio/create")


        } catch (e: Exception) {
            e.printStackTrace()
        }

        return lastAlignedFileUrl
    }

}

