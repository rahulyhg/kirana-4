package com.generic.core.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.generic.core.onboarding.exceldto.Excel;
import com.generic.core.onboarding.exceldto.ExcelSheetObject;
import com.generic.core.validation.functions.ValidationFunction;
import com.generic.core.validation.validate.GenericValidation;
import com.generic.rest.dto.ResponseDto;


public class ExcelUtilities {

	public static ExcelSheetObject readExcelSheet(String locationUrl, Class clazz, Boolean headerPresent) throws IOException, InstantiationException, IllegalAccessException {
		
		FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\pkonwar.ORADEV\\Dropbox\\kiranawala\\kiranaconnect\\Documents\\onboarding_filled_data\\shops_associate_with_items_onboarding_filled_sheet.xlsm"));
		ExcelSheetObject excelSheetObject  = new ExcelSheetObject();
		
		/*URL url = new URL(locationUrl);
		URLConnection uc = url.openConnection();
		XSSFWorkbook workbook = new XSSFWorkbook(uc.getInputStream());*/
		XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
		XSSFSheet sheet = workbook.getSheetAt(0);
		System.out.println("First Sheet: " + workbook.getSheetName(0));
		List<Object> excelSheetRows = new ArrayList<Object>();
		
		Object object = clazz.newInstance();
		Excel excelObject = (Excel)object;

		for (Row row : sheet) {
			if(row.getRowNum() == 0 && headerPresent)
				continue;

			//creating a new object row
			Object aRow = excelObject.createDataTypeObject(row);
			if(aRow != null) {
				excelSheetRows.add(aRow);
				System.out.println(aRow);
			}
		}
		excelSheetObject.setExcelSheetName(workbook.getSheetName(0));
		excelSheetObject.setRows(excelSheetRows);
		fileInputStream.close();
		return excelSheetObject;
	}
	
	
	public static List<ResponseDto> validate(Class clazz, List<? extends Object> list, Map<String, List<ValidationFunction>> rules) {
		
		GenericValidation validation  = new GenericValidation(clazz, list, rules);
		List<ResponseDto> results = null;
		try {
			results = validation.validate();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	
	public static void main(String[] args) {
		String urlStr = "https://dl.dropboxusercontent.com/u/37339694/kirana_onboarding.xlsm";
		URL url;
		try {
			url = new URL(urlStr);
			URLConnection uc = url.openConnection();
			XSSFWorkbook workbook = new XSSFWorkbook(uc.getInputStream());
			XSSFSheet sheet = workbook.getSheetAt(1);
			System.out.println("First Sheet: " + workbook.getSheetName(1));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
