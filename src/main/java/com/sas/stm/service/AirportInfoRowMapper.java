package com.sas.stm.service;

import java.sql.ResultSet;
import com.sas.stm.base.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import org.springframework.jdbc.core.RowMapper;

final class AirportInfoRowMapper implements RowMapper {

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * @return a {@link AirportInfo} object.
	 */
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		AirportInfo airInfo = new AirportInfo();

		airInfo.airportName = rs.getString("airportName");
		airInfo.airportCode = rs.getString("airportCode");
		airInfo.countryCode = rs.getString("countryCode");
		airInfo.latitude = rs.getString("latitude");
		airInfo.longitude = rs.getString("longitude");
		airInfo.cityCode = rs.getString("cityCode");
		airInfo.ICAOAirportCode = rs.getString("ICAOAirportCode");
		airInfo.countryName = rs.getString("countryName");
		airInfo.cityName = rs.getString("cityName");
		airInfo.TimeZone = rs.getString("TimeZone");


	
		return airInfo;
	}
}