/* putcolour.h
 * 
 * WBG 14-Oct-13
 * 
 * Public interface for putcolour.c
 * 
 * Implement coloured output on stdout's shell in either Linux (ANSI control codes) or
 * Windows (console text attributes).
 * 
 * There are two ways you can use these functions:
 * 
 * 1) Mode change:  Call setcolour(colour) to change the settings for subsequent output.  Call
 * setcolour(PC_AUTO) to go back to default colours, typically white text on black background. 
 * A specified colour will give white text on the coloured background, except PC_WHITE gives black
 * text on white background.
 * 
 * 2) Call putcolour() to output an entire buffer in the specified colour.  PC_AUTO mode prints "V" in
 * blue, ">" in red, and everything else in black-on-white.  putcolour() always goes back to default
 * colours after printing the buffer.
 * 
 * Non-windows output uses a bold attribute for better visibility against the coloured background.
 */

#ifndef PUTCOLOUR_H
#define PUTCOLOUR_H

/* putcolour - output a buffer on stdout in the specified colour
 * 
 * arguments:
 * 1) mode	specify colour mode for background:
 * 			PC_WHITE, PC_BLUE, PC_RED: uses coloured background for white text
 * 			PC_AUTO: blue for "v", red for ">", white for anything else
 * 2) buff	buffer of text to output on stdout
 * 3) blen	length of buffer to output, or <0 if buff is a NUL-terminated C string
 */
enum COL_MODE { PC_WHITE, PC_BLUE, PC_RED, PC_AUTO };
void putcolour( enum COL_MODE mode, const char *buff, int blen );

/* setcolour - change the colour mode for subsequent outputs on stdout
 * 
 * arguments:
 * 1) mode	as for putcolour, except AUTO restores the default colours
 */
void setcolour( enum COL_MODE mode );

#endif
