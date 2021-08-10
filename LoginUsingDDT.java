package scripts.ddt;

import org.testng.annotations.Test;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.testng.annotations.BeforeMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;

public class LoginUsingDDT {
	WebDriver chromDriver;

	@Test(dataProvider = "CSV")
	public void nTLoginTest(String userID, String password) throws InterruptedException {
		driver.get("https://nichethyself.com/tourism/home.html");
		driver.findElement(By.name("username")).sendKeys(userID);
		driver.findElement(By.name("password")).sendKeys(password);
//		driver.findElement(By.name("password")).submit();
//		Thread.sleep(3000);
	}

	@BeforeMethod
	public void beforeMethod() {
		System.setProperty("webdriver.chrome.driver", "test\\resources\\chromedriver.exe");
		driver = new ChromeDriver();
	}

	@AfterMethod
	public void afterMethod() {
		driver.quit();
	}

	@DataProvider(name = "Cred")
	public Object[][] getDataFromExternalSource() {
		Object[][] retObjArr = readDataFromXLS("test\\resources\\data\\Credentials.xls", "NT", "NTStartEnd");
		return (retObjArr);
		// retObjArr[0][0] = 'stc123'
		// retObjArr[0][1] = '12345'
		// retObjArr[1][0] = 'testing'
		// retObjArr[1][1] = 'testing'
		// retObjArr[2][0] = 'niche'
		// retObjArr[2][1] = 'n123'

	}

	@DataProvider(name = "CSV")
	public Iterator getDataFromExternalSourceCSV() throws IOException {
		Collection<String[]> retObjArr = getTestData("test\\resources\\data\\Datacsv.csv");
		return (retObjArr.iterator());
	}

	public static Collection<String[]> getTestData(String fileName) throws IOException {
		List<String[]> records = new ArrayList<String[]>();
		String record;
		// FileReader is meant for reading streams of characters(reading text
		// files)
		// FileReader myReader = new FileReader(fileName);
		// A BufferedReader object takes a FileReader object as
		// an input which contains all the necessary information
		// about the text file that needs to be read.
		BufferedReader file = new BufferedReader(new FileReader(fileName));
		while ((record = file.readLine()) != null) {// record = "stc123,12345"
			String fields[] = record.split(",");// split() method is inside
												// String class
			records.add(fields);
		}
		file.close();
		return records;
	}

	public String[][] readDataFromXLS(String xlFilePath, String sheetName, String marker) {
		String[][] tabArray = null;
		try {
			Workbook workbook = Workbook.getWorkbook(new File(xlFilePath));
			// Workbook class is provied by jxl.jar
			// WebDriver provided by Selenium
			// File is class provided by Java to read a physical file
			Sheet sheet = workbook.getSheet(sheetName);
			Cell tableStart = sheet.findCell(marker);

			int startRow, startCol, endRow, endCol, ci, cj;

			startRow = tableStart.getRow();// 2
			startCol = tableStart.getColumn();// 1

			Cell tableEnd = sheet.findCell(marker, startCol + 1, startRow + 1, 100, 64000, false);

			endRow = tableEnd.getRow();// 8
			endCol = tableEnd.getColumn();// 4
			System.out.println("startRow=" + startRow + ", endRow=" + endRow + ", " + "startCol=" + startCol
					+ ", endCol=" + endCol);
			tabArray = new String[endRow - startRow - 1][endCol - startCol - 1];// 5,
																				// 2
			ci = 0; // array row
			// ci=0,i=3, j=3,cj=1
			for (int i = startRow + 1; i < endRow; i++, ci++) {// i represents
																// xls row
				cj = 0;// array column
				for (int j = startCol + 1; j < endCol; j++, cj++) {// j
																	// represents
																	// xls
																	// column
					tabArray[ci][cj] = sheet.getCell(j, i).getContents();
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("The file you specified does not exist");
		} catch (Exception e) {
			System.out.println("Please check if file path, sheet name and tag name are correct");
			e.toString();

		}

		return (tabArray);
	}

	public static Collection<String[]> getTestData(String mdbFile, String sqlQuery) throws Exception {
		ArrayList<String[]> records = new ArrayList<String[]>();

		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");// Driver for MSAccess
		String myDB = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + mdbFile;// Database
																							// String
		Connection conn = DriverManager.getConnection(myDB, "", "");

		/*
		 * Class.forName("com.mysql.jdbc.Driver"); String myDB =
		 * "jdbc:mysql://382.43.2.4:3306/paratus"; String myDB =
		 * "jdbc:mysql://localhost:3306/paratus";//paratus is a DB name which
		 * one needs to create
		 * conn1=DriverManager.getConnection(myDB,"mysql","mysql"); or below
		 * conn1=DriverManager.getConnection(myDB,"root","");
		 */
		Statement stmt = null; // SQL Statements

		ResultSet rs = null; // DB data are returned inside ResultSet object

		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		rs = stmt.executeQuery(sqlQuery);
		ResultSetMetaData rsMetaData = rs.getMetaData();

		int cols = rsMetaData.getColumnCount();
		// Connection, DriverManager,Statement, ResultSet, ResultSetMetaData
		while (rs.next()) {

			String fields[] = new String[cols]; // creating array of columns
			int col = 0;
			for (int colIdx = 1; colIdx <= cols; colIdx++) {
				fields[col] = rs.getString(colIdx);
				col++;
			}
			records.add(fields);
		}

		rs.close();
		stmt.close();
		conn.close();

		return records;
	}

}
