package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;

public class DisplayType extends ConfigurationObject 
{

	@JsonProperty("descr") 
    String descr;
	
	@JsonProperty("pixels_width") 
    Integer pixelsWidth;
	
	@JsonProperty("pixels_height") 
    Integer pixelsHeight;
	
	@JsonProperty("text_color") 
    String textColor;
	
	@JsonProperty("back_color") 
    String backColor;
	
	@JsonProperty("in_mode") 
    String inMode;
	
	@JsonProperty("out_mode") 
    String outMode;
	
	@JsonProperty("speed") 
    String speed;
	
	@JsonProperty("line_spacing") 
    String lineSpacing;

	@JsonProperty("letter_size") 
	String letterSize;
	
	@JsonProperty("vertical_alignment") 
    String verticalAlignment;
	
	@JsonProperty("horizontal_alignment") 
    String horizontalAlignment;
    
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime createDate;

	@JsonCreator
	public DisplayType(@JsonProperty("id") Integer id) {
		super(id);
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public Integer getPixelsWidth() {
		return pixelsWidth;
	}

	public void setPixelsWidth(Integer pixelsWidth) {
		this.pixelsWidth = pixelsWidth;
	}

	public Integer getPixelsHeight() {
		return pixelsHeight;
	}

	public void setPixelsHeight(Integer pixelsHeight) {
		this.pixelsHeight = pixelsHeight;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getBackColor() {
		return backColor;
	}

	public void setBackColor(String backColor) {
		this.backColor = backColor;
	}

	public String getInMode() {
		return inMode;
	}

	public void setInMode(String inMode) {
		this.inMode = inMode;
	}

	public String getOutMode() {
		return outMode;
	}

	public void setOutMode(String outMode) {
		this.outMode = outMode;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getLineSpacing() {
		return lineSpacing;
	}

	public void setLineSpacing(String lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	public String getLetterSize() {
		return letterSize;
	}

	public void setLetterSize(String letterSize) {
		this.letterSize = letterSize;
	}

	public String getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(String verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	public String getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(String horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public LocalDateTime getCreateDate() {
		return createDate;
	}

	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			// TODO: log the error
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return jsonInString;
	}
}
