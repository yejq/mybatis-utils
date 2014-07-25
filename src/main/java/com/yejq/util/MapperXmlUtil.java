package com.yejq.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.junit.Test;

/**
 * 生成mapper.xml的辅助工具类
 * 
 * @author jim.ye
 */
public class MapperXmlUtil {

	private static final String CURRENT_PATH = "D:/workspace/mybatis-utils/src/main/java/com/yejq/util/";
	public static final String FILE_INPUT = CURRENT_PATH + "in.txt";
	public static final String FILE_OUTPUT = CURRENT_PATH + "out.txt";

	Map<String, String> filters = new HashMap<String, String>();

	public MapperXmlUtil() throws Exception {
		File inputFile = new File(FILE_INPUT);
		inputFile.createNewFile();
		File outputFile = new File(FILE_OUTPUT);
		outputFile.createNewFile();
		filters.put("id", "");
		filters.put("last_update_time", "");
	}
	
	/**
	 * 通过mysql的建表语句生成mapper.xml里边的inert语句
	 * @param 
	 * @return void
	 * @throws
	 */
	@Test
	public void dbToInsertSql() throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader(FILE_INPUT));
		String line = "";
		Vector<KeyObject> keys = new Vector<KeyObject>();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.startsWith(MyConstants.ESCAPE_STRING)) {
				String keyName = line.substring(1);
				keyName = keyName.substring(0, keyName.indexOf(MyConstants.ESCAPE_STRING));
				if (!filters.containsKey(keyName)) {
					KeyObject keyObject = new KeyObject();
					keyObject.setKey(keyName);
					String typeString = line.substring(line.indexOf(keyName + MyConstants.ESCAPE_STRING) + (keyName + MyConstants.ESCAPE_STRING).length()).trim();
					if (typeString.indexOf("(") > -1) {
						typeString = typeString.substring(0, typeString.indexOf("("));
					}else {
						typeString = typeString.substring(0, typeString.indexOf(" "));
					}
					keyObject.setType(typeString);
					keys.add(keyObject);
				}
			}
		}
		reader.close();
		StringBuffer bracketBuffer = new StringBuffer("(");
		String sperator = ",";
		int j = 0;
		for (KeyObject keyObject : keys) {
			bracketBuffer.append(keyObject.getKey());
			if (j < keys.size() - 1) {
				bracketBuffer.append(sperator).append(" ");
			}
			j++;
		}
		bracketBuffer.append(")");
		System.out.println(bracketBuffer);
		
		StringBuffer chooseBuffer = new StringBuffer("");
		j = 0;
		String pattern = MyConstants.CHOOSE_FORMAT_STRING;
		for (KeyObject keyObject: keys) {
			switch (keyObject.getType()) {
			case "int":
				pattern = MyConstants.CHOOSE_FORMAT_INT;
				break;
			case "tinyint":
				pattern = MyConstants.CHOOSE_FORMAT_INT;
				break;	
			case "timestamp":
				pattern = MyConstants.CHOOSE_FORMAT_TIME_DEFAULT;
				break;
			case "decimal":
				pattern = MyConstants.CHOOSE_FORMAT_DECIMAL;
				break;
			default:
				pattern = MyConstants.CHOOSE_FORMAT_STRING;
				break;
			}
			if ("create_time".equals(keyObject.getKey())) {
				pattern = MyConstants.CHOOSE_FORMAT_TIME_NOW;
			}
			String javaString = chgDBColumnToJavaProperty(keyObject.getKey());
			chooseBuffer.append(MessageFormat.format(pattern, javaString, javaString));
			chooseBuffer.append(MyConstants.NEWLINE);
		}
		System.out.println(chooseBuffer);
		
		BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(FILE_OUTPUT, "UTF-8"));
		writer.write(bracketBuffer.toString() + MyConstants.NEWLINE + chooseBuffer.toString());
		writer.close();
	}
	
	@Test
	public void testChgDBColumnToJavaProperty(){
		//System.out.println(chgDBColumnToJavaProperty("operate_company_id"));
	}
	
	private String chgDBColumnToJavaProperty(String columnName){
		String[] columnNames = columnName.split("_");
		String javaProperty = columnNames[0];
		for (int i = 1; i < columnNames.length; i++) {
			javaProperty += columnNames[i].substring(0, 1).toUpperCase() + columnNames[i].substring(1);
		}
		return javaProperty;
	}
}

class KeyObject{
	private String key;
	private String type;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
