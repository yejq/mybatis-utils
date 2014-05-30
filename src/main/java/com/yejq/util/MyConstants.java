package com.yejq.util;

public class MyConstants {
	public static final String ESCAPE_STRING = "`";
	public static final String NEWLINE = System.getProperty("line.separator");
	
	public static final String CHOOSE_FORMAT_STRING = 
   "			<choose>" + NEWLINE
  +"				<when test=\"item.{0} == null\">'''',</when>" + NEWLINE
  +"				<otherwise>#'{'item.{1}'}',</otherwise>" + NEWLINE
  +"			</choose>";
	public static final String CHOOSE_FORMAT_INT = 
   "			<choose>" + NEWLINE
  +"				<when test=\"item.{0} == null\">0,</when>" + NEWLINE
  +"				<otherwise>#'{'item.{1}'}',</otherwise>" + NEWLINE
  +"			</choose>";
	public static final String CHOOSE_FORMAT_TIME_DEFAULT = 
   "			<choose>" + NEWLINE
  +"				<when test=\"item.{0} == null\">'0000-00-00 00:00:00',</when>" + NEWLINE
  +"				<otherwise>#'{'item.{1}'}',</otherwise>" + NEWLINE
  +"			</choose>";
	public static final String CHOOSE_FORMAT_TIME_NOW = 
   "			<choose>" + NEWLINE
  +"				<when test=\"item.{0} == null\">now(),</when>" + NEWLINE
  +"				<otherwise>#'{'item.{1}'}',</otherwise>" + NEWLINE
  +"			</choose>";
	public static final String CHOOSE_FORMAT_DECIMAL = 
			   "			<choose>" + NEWLINE
			  +"				<when test=\"item.{0} == null\">0.0000,</when>" + NEWLINE
			  +"				<otherwise>#'{'item.{1}'}',</otherwise>" + NEWLINE
			  +"			</choose>";
}
