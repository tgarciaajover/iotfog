package com.advicetec.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.advicetec.core.serialization.LocalDateTimeDeserializer;
import com.advicetec.core.serialization.LocalDateTimeSerializer;


/**
 * This class represents an specific display that can be used to show information. Among other things that can be configured are:
 * 
 *    - layout in pixels
 *    - Colors.
 * 
 * @author maldofer
 *
 */
public class DisplayType extends ConfigurationObject 
{

	static Logger logger = LogManager.getLogger(DisplayType.class.getName());
	
	/**
	 * Device type description.
	 */
	@JsonProperty("descr") 
    String descr;
	
	/**
	 * width in pixels.
	 */
	@JsonProperty("pixels_width") 
    Integer pixelsWidth;
	
	/**
	 * height in pixels.
	 */
	@JsonProperty("pixels_height") 
    Integer pixelsHeight;
	
	/**
	 * text color
	 */
	@JsonProperty("text_color") 
    String textColor;
	
	/**
	 * background color
	 */
	@JsonProperty("back_color") 
    String backColor;
	
	/**
	 * 
	 */
	@JsonProperty("in_mode") 
    String inMode;
	
	/**
	 * 
	 */
	@JsonProperty("out_mode") 
    String outMode;
	
	/**
	 * speed in which the messages go though the display/
	 */
	@JsonProperty("speed") 
    String speed;
	
	/**
	 * Line spacing
	 */
	@JsonProperty("line_spacing") 
    Integer lineSpacing;

	/**
	 * letter size
	 */
	@JsonProperty("letter_size") 
	String letterSize;
	
	/**
	 * vertical alignment
	 */
	@JsonProperty("vertical_alignment") 
    String verticalAlignment;
	
	/**
	 * horizontal alignment
	 */
	@JsonProperty("horizontal_alignment") 
    String horizontalAlignment;
    
	/**
	 * datatime when the display type was created.
	 */
	@JsonProperty("create_date") 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)	
    private LocalDateTime createDate;

	/**
	 * Constructor for the class.
	 * 
	 * @param id identifier of the display type.
	 */
	@JsonCreator
	public DisplayType(@JsonProperty("id") Integer id) {
		super(id);
	}

	/**
	 * Gets the display type description.
	 * 
	 * @return description.
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the display type description.
	 * 
	 * @param descr  description to be set.
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the Width in pixels  
	 * 
	 * @return  Width in pixels  
	 */
	public Integer getPixelsWidth() {
		return pixelsWidth;
	}

	/**
	 * Sets the Width in pixels  
	 * 
	 * @param pixelsWidth  Width in pixels  
	 */
	public void setPixelsWidth(Integer pixelsWidth) {
		this.pixelsWidth = pixelsWidth;
	}

	/**
	 * Gets the Height in pixels
	 * 
	 * @return  Height in pixels
	 */
	public Integer getPixelsHeight() {
		return pixelsHeight;
	}

	/**
	 * Sets the Height in pixels
	 * 
	 * @param pixelsHeight   Height in pixels
	 */
	public void setPixelsHeight(Integer pixelsHeight) {
		this.pixelsHeight = pixelsHeight;
	}

	/**
	 * Gets the text color
	 * 
	 * @return  text color
	 */
	public String getTextColor() {
		return textColor;
	}

	/**
	 * Sets the text color
	 * 
	 * @param textColor  text color to set.
	 */
	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	/**
	 * Gets the background color
	 * 
	 * @return background color
	 */
	public String getBackColor() {
		return backColor;
	}

	/**
	 * Sets the background color 
	 * 
	 * @param backColor  background color
	 */
	public void setBackColor(String backColor) {
		this.backColor = backColor;
	}

	/**
	 * 
	 * @return
	 */
	public String getInMode() {
		return inMode;
	}

	/**
	 * @param inMode
	 */
	public void setInMode(String inMode) {
		this.inMode = inMode;
	}

	/**
	 * @return
	 */
	public String getOutMode() {
		return outMode;
	}

	/**
	 * @param outMode
	 */
	public void setOutMode(String outMode) {
		this.outMode = outMode;
	}

	/**
	 * Gets the speed used for messages.
	 * @return message speed 
	 */
	public String getSpeed() {
		return speed;
	}

	/**
	 * Sets the speed used for messages.
	 * @param speed  message speed
	 */
	public void setSpeed(String speed) {
		this.speed = speed;
	}

	/**
	 * Gets the line spacing
	 * 
	 * @return  line spacing
	 */
	public Integer getLineSpacing() {
		return lineSpacing;
	}

	/**
	 * Sets the line spacing
	 * 
	 * @param lineSpacing  line spacing
	 */
	public void setLineSpacing(Integer lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	/**
	 * Gets the letter size
	 * 
	 * @return  letter size
	 */
	public String getLetterSize() {
		return letterSize;
	}

	/**
	 * Sets the letter size
	 * 
	 * @param letterSize  letter size
	 */
	public void setLetterSize(String letterSize) {
		this.letterSize = letterSize;
	}

	/**
	 * Gets the vertical alignment
	 * 
	 * @return  vertical alignment
	 */
	public String getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * Sets the vertical alignment
	 * @param verticalAlignment  vertical alignment
	 */
	public void setVerticalAlignment(String verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * Gets the horizontal alignment
	 * @return horizontal alignment
	 */
	public String getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * Sets the horizontal alignment
	 * 
	 * @param horizontalAlignment  horizontal alignment
	 */
	public void setHorizontalAlignment(String horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 * Gets the create date 
	 * @return  create date
	 */
	public LocalDateTime getCreateDate() {
		return createDate;
	}

	/**
	 * Sets the create date
	 * 
	 * @param createDate create date
	 */
	public void setCreateDate(LocalDateTime createDate) {
		this.createDate = createDate;
	}

	/**
	 * Creates an Json object representing the display type
	 * 
	 * @return Json object representing the display type
	 */
	public String toJson()
	{
		ObjectMapper mapper = new ObjectMapper();
			
		String jsonInString=null;
		try {
			
			jsonInString = mapper.writeValueAsString(this);
			
			
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return jsonInString;
	}
}
