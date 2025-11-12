package sk.emaa.dto;

import java.time.LocalDate;

public class AttendanceDto {
	
	public int id;
	public int studentId;
	public int trainingId;
	public String firstname;
	public String lastname;
	public boolean present;
	public LocalDate date;
	public int credit;

}
