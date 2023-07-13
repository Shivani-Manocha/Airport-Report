package com.sas.stm.service;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.sas.stm.base.AirportInfo;

@SpringBootApplication
@EnableAutoConfiguration
@Component
public class AirportReportService
//extends SpringBootServletInitializer
		implements ApplicationRunner {

	@Value("${csv.file.path}")
	private String filePath;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	// ----------------------------------------------------------------//
	// SQL Query to fetch an airport details from the flight suite db
	// ---------------------------------------------------------------//
	private final static String selectAirportData = "SELECT s.NAME as airportName, s.STN as airportCode, s.COUNTRY as countryCode, s.LATITUDE as latitude, s.CITYNAME as cityName,"
			+ " s.LONGITUDE as longitude, s.CITY as cityCode, s.ICAO as ICAOAirportCode, s.TIMEZONE as TimeZone, c.Name as countryName"
			+ " FROM station s WITH (NOLOCK) inner join COUNTRY c WITH (NOLOCK) on s.COUNTRY = c.COUNTRY inner join TIMEZONE t WITH (NOLOCK) on s.TIMEZONE = t.TIMEZONE and s.COUNTRY = t.COUNTRY";

	private static final Logger logger = LoggerFactory.getLogger(AirportReportService.class);
	
	@Override
	public void run(ApplicationArguments args) {
		try {
			logger.info("selectAirportData query :- " + selectAirportData);

			// --------------------------------------------------------//
			// read the data from the database
			// --------------------------------------------------------//
			List airportData = jdbcTemplate.query(selectAirportData, new AirportInfoRowMapper());
			logger.info(" Successfully read a record from the flight suite db");

			// ----------------------------------------------------------------------//
			// Function which will arrange db data and create a csv file
			// ----------------------------------------------------------------------//
			arrangeAirportData(airportData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("error:" + e.toString());
		}
	}

	// ------------------------------------------------------------------------------------------------------//
	// Function which will create a csv file, filter the db data and arrange the
	// data into the csv file
	// ------------------------------------------------------------------------------------------------------//
	private void arrangeAirportData(List airportData) throws IOException {

		BufferedWriter bw = null;
		// ----------------------------------//
		// get the current date
		// ---------------------------------//
		String fileDate = getCurrentDate();
		logger.info("FileDate :- " + fileDate);
		try {
			logger.info("Creating a csv file :- ");
			
			// ------------------------------------------------------------------------------------------------------//
			// Create a new csv file. The file naming is created based on the current date
			// ------------------------------------------------------------------------------------------------------//
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(filePath+"/airport_"+fileDate+".csv"), "UTF-16LE"));
			bw.write(
					"AirportName, AirportCode, CountryCode, Latitude, Longitude, CityCode, ICAOAAirportCode, cityName, TimeZone, countryName");
			bw.newLine();
			logger.info("File created successfully :- ");
			for (int record = 0; record < airportData.size(); record++) {
				
				logger.info("Iterating flight suit data");
				AirportInfo singleRecord = (AirportInfo) airportData.get(record);
				
				// ------------------------------------------------------------------------------------------------------//
				// If the data which we are reading from the flightsuit db already separated
				// from the comma then
				// replace it with space
				// ------------------------------------------------------------------------------------------------------//
				singleRecord.cityName = singleRecord.cityName.contains(",") ? singleRecord.cityName.replace(",", " ")
						: singleRecord.cityName;
				singleRecord.countryName = singleRecord.countryName.contains(",")
						? singleRecord.countryName.replace(",", " ")
						: singleRecord.countryName;
				singleRecord.airportName = singleRecord.airportName.contains(",")
						? singleRecord.airportName.replace(",", " ")
						: singleRecord.airportName;

				// ---------------------------------------------------------------//
				// call the function which will write data into the csv file
				// --------------------------------------------------------------//
				addRecordInCSVFile(singleRecord,bw);
			}

		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException :- " + e.getMessage());
		} catch (IOException e) {
			logger.error("IOException :- " + e.getMessage());
		} catch(Exception e) { 
			logger.error("Exception :- " + e.getMessage());
		}finally {
			bw.flush();
			bw.close();
			logger.info("closed BufferedWriter");
		}
	}

	// --------------------------------------------------------------//
	// Function which will add data into the csv file
	// -------------------------------------------------------------//
	private void addRecordInCSVFile(AirportInfo Record, BufferedWriter bw) {
		
		logger.info("Adding date into the csv file");
		final String CSV_SEPARATOR = ",";
		try {
			StringBuffer strObject = new StringBuffer();
			strObject.append(Record.airportName.trim().length() == 0 ? "" : Record.airportName);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.airportCode.trim().length() == 0 ? "" : Record.airportCode);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.countryCode.trim().length() == 0 ? "" : Record.countryCode);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.latitude.trim().length() == 0 ? "" : Record.latitude);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.longitude.trim().length() == 0 ? "" : Record.longitude);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.cityCode.trim().length() == 0 ? "" : Record.cityCode);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.ICAOAirportCode.trim().length() == 0 ? "" : Record.ICAOAirportCode);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.cityName.trim().length() == 0 ? "" : Record.cityName);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.TimeZone.trim().length() == 0 ? "" : Record.TimeZone);
			strObject.append(CSV_SEPARATOR);

			strObject.append(Record.countryName.trim().length() == 0 ? "" : Record.countryName);
			bw.write(strObject.toString());
			logger.info(strObject.toString());
			bw.newLine();
		} catch (Exception e) {
			logger.error("data not added into csv file " + e.getMessage());
		}
	}

	// --------------------------------------------------------------//
	// Function which will return the current date
	// -------------------------------------------------------------//
	public String getCurrentDate() {

		logger.info("read current date");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("ddMMMyyyy");
		LocalDateTime current = LocalDateTime.now();
		String currentDate = format.format(current).toUpperCase();

		return currentDate;
	}
}
