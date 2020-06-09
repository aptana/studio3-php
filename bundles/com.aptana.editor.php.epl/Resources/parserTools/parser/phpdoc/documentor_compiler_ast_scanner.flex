/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/

package org2.eclipse.php.internal.core.compiler.ast.parser;

import java.io.IOException;
import java.util.ArrayList;

import org2.eclipse.php.internal.core.Logger;
import org2.eclipse.php.internal.core.ast.nodes.IDocumentorLexer;
import org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTag;
import org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTagKinds;

%%

%class DocumentorLexer
%public
%implements IDocumentorLexer
%unicode
%line

%eofclose

%caseless
%function next_token

%standalone

%state ST_IN_SHORT_DESC
%state ST_IN_LONG_DESC
%state ST_IN_TAGS


%{
    private String shortDesc = null;
    private String longDesc = null;
    private ArrayList<PHPDocTag> tagList = null;
    private int currTagId = 0;
    private int tagPosition = 0;
    private StringBuffer sBuffer = null;
    private int numOfLines = 0;
    private int startPos = 0;

    public PHPDocBlock parse (){
    	int start = zzStartRead - zzPushbackPos;
        longDesc = "";
        tagList = new ArrayList<PHPDocTag>();
        sBuffer = new StringBuffer();
        numOfLines = 1;

        //start parsing
        try {
            next_token();
        } catch (IOException e) {
            Logger.logException(e);
        }

        PHPDocTag[] tags = new PHPDocTag[tagList.size()];
        tagList.toArray(tags);

        PHPDocBlock rv = new PHPDocBlock(start, zzMarkedPos - zzPushbackPos, shortDesc, tags);

        return rv;

    }

    private void startTagsState(int firstState){
        updateStartPos();
        hendleDesc();
        currTagId = firstState;
        tagPosition = findTagPosition();
        sBuffer = new StringBuffer();
        yybegin(ST_IN_TAGS);
    }

    private int findTagPosition(){
    	for (int i = zzStartRead; i < zzMarkedPos; i++) {
			if(zzBuffer[i]=='@'){
				return i - zzPushbackPos;
			}
		}
    	return -1;
    }
    private void setNewTag(int newTag){
       updateStartPos();
       setTagValue();

       sBuffer = new StringBuffer();
       currTagId = newTag;
       tagPosition = findTagPosition();
    }

    private void setTagValue(){
        String value = sBuffer.toString();
        // special case for backward compatibility
        if (currTagId == PHPDocTagKinds.DESC) {
            shortDesc = shortDesc + value;
            return;
        }

        PHPDocTag basicPHPDocTag = new PHPDocTag(tagPosition, zzStartRead - zzPushbackPos, currTagId,value);
        tagList.add(basicPHPDocTag);
    }

    private void appendText(){
    	if(oldString != null){
    		sBuffer.append(oldString);
    	}
       	sBuffer.append(zzBuffer, startPos, zzMarkedPos-startPos);
       	updateStartPos();
    }

    private void hendleDesc() {
        if(zzLexicalState == ST_IN_SHORT_DESC){
            shortDesc = sBuffer.toString().trim();
        }
        else{
            longDesc = sBuffer.toString().trim();
        }

        sBuffer = new StringBuffer();
    }

    private void startLongDescState() {
        hendleDesc();
        updateStartPos();
        yybegin(ST_IN_LONG_DESC);
    }

    private void hendleNewLine() {
        appendText();
        if(numOfLines==4){
            int firstLineEnd = sBuffer.indexOf("\n",1);
            shortDesc = sBuffer.substring(0,firstLineEnd);
            shortDesc = shortDesc.trim();
            sBuffer.delete(0,firstLineEnd);
            yybegin(ST_IN_LONG_DESC);
        }
        else{
          numOfLines++;
        }
    }

    private void appendLastText(){
       sBuffer.append(zzBuffer, startPos, zzMarkedPos-startPos-2);
       updateStartPos();
    }

    int maxNumberofLines = 4;

    private void handleDocEnd_shortDesc() {
        appendLastText();
        if(numOfLines==maxNumberofLines){
            int firstLineEnd = sBuffer.indexOf("\n",1);
            shortDesc = sBuffer.substring(0,firstLineEnd);
            shortDesc = shortDesc.trim();
            sBuffer.delete(0,firstLineEnd);
            longDesc = sBuffer.toString().trim();
        }
        else{
            shortDesc = sBuffer.toString().trim();
        }
    }

    private void handleDocEnd_longDesc() {
       appendLastText();
       longDesc = sBuffer.toString().trim();
    }

    private void handleDocEnd_inTags() {
        appendLastText();
        setTagValue();
    }


    private void updateStartPos(){
        startPos = zzMarkedPos;
        oldString = null;
    }
    
    public void reset(java.io.Reader  reader, char[] buffer, int[] parameters){
    	this.zzReader = reader;
    	this.zzBuffer = buffer;
    	this.zzMarkedPos = parameters[0];
    	this.zzPushbackPos = parameters[1];
    	this.zzCurrentPos = parameters[2];
    	this.zzStartRead = parameters[3];
    	this.zzEndRead = parameters[4];
    	this.yyline = parameters[5];  
    }
    
    public int[] getParamenters(){
    	return new int[]{zzMarkedPos, zzPushbackPos, zzCurrentPos, zzStartRead, zzEndRead, yyline, zzLexicalState};
    }
    
    public char[] getBuffer(){
    	return zzBuffer;
    }
    
%}

TABS_AND_SPACES=[ \t]*
ANY_CHAR=(.|[\n])
NEWLINE=("\r"|"\n"|"\r\n")
LINESTART=({TABS_AND_SPACES}"*"?{TABS_AND_SPACES})
EMPTYLINE=({LINESTART}{TABS_AND_SPACES}{NEWLINE})


%%

<YYINITIAL> {
    ^"/**"{TABS_AND_SPACES}({NEWLINE}) {
        updateStartPos();
        yybegin(ST_IN_SHORT_DESC);
    }
    ^"/**"{TABS_AND_SPACES} {
        updateStartPos();
        yybegin(ST_IN_SHORT_DESC);
    }
}

<YYINITIAL>{ANY_CHAR}   {}

<ST_IN_SHORT_DESC>^{TABS_AND_SPACES}("*/") {
    maxNumberofLines = 5;
    handleDocEnd_shortDesc();
    return -1;
}
<ST_IN_SHORT_DESC>{TABS_AND_SPACES}("*/") {
    maxNumberofLines = 4;
    handleDocEnd_shortDesc();
    return -1;
}

<ST_IN_SHORT_DESC>^{EMPTYLINE}  {startLongDescState();}

<ST_IN_SHORT_DESC>(([ \t]+)"."|"."([ \t]+)|"."{NEWLINE}) {
    appendText();
    startLongDescState();
}

<ST_IN_SHORT_DESC>{NEWLINE}     {hendleNewLine();}
<ST_IN_SHORT_DESC>.             {}

<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@abstract")     	{startTagsState(PHPDocTagKinds.ABSTRACT);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@access")       	{startTagsState(PHPDocTagKinds.ACCESS);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@author")       	{startTagsState(PHPDocTagKinds.AUTHOR);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@category")     	{startTagsState(PHPDocTagKinds.CATEGORY);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@copyright")    	{startTagsState(PHPDocTagKinds.COPYRIGHT);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@deprecated")   	{startTagsState(PHPDocTagKinds.DEPRECATED);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@desc")         	{startTagsState(PHPDocTagKinds.DESC);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@example")      	{startTagsState(PHPDocTagKinds.EXAMPLE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@final")       		{startTagsState(PHPDocTagKinds.FINAL);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@filesource")   	{startTagsState(PHPDocTagKinds.FILESOURCE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@global")       	{startTagsState(PHPDocTagKinds.GLOBAL);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@ignore")       	{startTagsState(PHPDocTagKinds.IGNORE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@internal")     	{startTagsState(PHPDocTagKinds.INTERNAL);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@license")      	{startTagsState(PHPDocTagKinds.LICENSE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@link")         	{startTagsState(PHPDocTagKinds.LINK);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@method")         	{startTagsState(PHPDocTagKinds.METHOD);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@name")         	{startTagsState(PHPDocTagKinds.NAME);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@package")      	{startTagsState(PHPDocTagKinds.PACKAGE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@param")        	{startTagsState(PHPDocTagKinds.PARAM);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@return")       	{startTagsState(PHPDocTagKinds.RETURN);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@see")          	{startTagsState(PHPDocTagKinds.SEE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@since")        	{startTagsState(PHPDocTagKinds.SINCE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@static")       	{startTagsState(PHPDocTagKinds.STATIC);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@staticvar")    	{startTagsState(PHPDocTagKinds.STATICVAR);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@subpackage")   	{startTagsState(PHPDocTagKinds.SUBPACKAGE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@throws")       	{startTagsState(PHPDocTagKinds.THROWS);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@todo")         	{startTagsState(PHPDocTagKinds.TODO);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@tutorial")     	{startTagsState(PHPDocTagKinds.TUTORIAL);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@uses")         	{startTagsState(PHPDocTagKinds.USES);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@var")          	{startTagsState(PHPDocTagKinds.VAR);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@uses")         	{startTagsState(PHPDocTagKinds.USES);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@property")     	{startTagsState(PHPDocTagKinds.PROPERTY);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@property-read")  	{startTagsState(PHPDocTagKinds.PROPERTY_READ);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@property-write") 	{startTagsState(PHPDocTagKinds.PROPERTY_WRITE);}
<ST_IN_SHORT_DESC,ST_IN_LONG_DESC>^{LINESTART}("@version")      	{startTagsState(PHPDocTagKinds.VERSION);}

<ST_IN_SHORT_DESC,ST_IN_LONG_DESC,ST_IN_TAGS>^{LINESTART}       {updateStartPos();}

<ST_IN_LONG_DESC>{TABS_AND_SPACES}("*/") {handleDocEnd_longDesc();return -1;}

<ST_IN_LONG_DESC>{NEWLINE}   {appendText();}

<ST_IN_LONG_DESC>.             {}


<ST_IN_TAGS>^{LINESTART}("@abstract")   	{setNewTag(PHPDocTagKinds.ABSTRACT);}
<ST_IN_TAGS>^{LINESTART}("@access")     	{setNewTag(PHPDocTagKinds.ACCESS);}
<ST_IN_TAGS>^{LINESTART}("@author")     	{setNewTag(PHPDocTagKinds.AUTHOR);}
<ST_IN_TAGS>^{LINESTART}("@category")   	{setNewTag(PHPDocTagKinds.CATEGORY);}
<ST_IN_TAGS>^{LINESTART}("@copyright")  	{setNewTag(PHPDocTagKinds.COPYRIGHT);}
<ST_IN_TAGS>^{LINESTART}("@deprecated") 	{setNewTag(PHPDocTagKinds.DEPRECATED);}
<ST_IN_TAGS>^{LINESTART}("@desc")       	{setNewTag(PHPDocTagKinds.DESC);}
<ST_IN_TAGS>^{LINESTART}("@example")    	{setNewTag(PHPDocTagKinds.EXAMPLE);}
<ST_IN_TAGS>^{LINESTART}("@final")      	{setNewTag(PHPDocTagKinds.FINAL);}
<ST_IN_TAGS>^{LINESTART}("@filesource") 	{setNewTag(PHPDocTagKinds.FILESOURCE);}
<ST_IN_TAGS>^{LINESTART}("@global")     	{setNewTag(PHPDocTagKinds.GLOBAL);}
<ST_IN_TAGS>^{LINESTART}("@ignore")     	{setNewTag(PHPDocTagKinds.IGNORE);}
<ST_IN_TAGS>^{LINESTART}("@internal")   	{setNewTag(PHPDocTagKinds.INTERNAL);}
<ST_IN_TAGS>^{LINESTART}("@license")    	{setNewTag(PHPDocTagKinds.LICENSE);}
<ST_IN_TAGS>^{LINESTART}("@link")       	{setNewTag(PHPDocTagKinds.LINK);}
<ST_IN_TAGS>^{LINESTART}("@method")       	{setNewTag(PHPDocTagKinds.METHOD);}
<ST_IN_TAGS>^{LINESTART}("@name")       	{setNewTag(PHPDocTagKinds.NAME);}
<ST_IN_TAGS>^{LINESTART}("@package")    	{setNewTag(PHPDocTagKinds.PACKAGE);}
<ST_IN_TAGS>^{LINESTART}("@param")      	{setNewTag(PHPDocTagKinds.PARAM);}
<ST_IN_TAGS>^{LINESTART}("@return")     	{setNewTag(PHPDocTagKinds.RETURN);}
<ST_IN_TAGS>^{LINESTART}("@see")        	{setNewTag(PHPDocTagKinds.SEE);}
<ST_IN_TAGS>^{LINESTART}("@since")      	{setNewTag(PHPDocTagKinds.SINCE);}
<ST_IN_TAGS>^{LINESTART}("@static")     	{setNewTag(PHPDocTagKinds.STATIC);}
<ST_IN_TAGS>^{LINESTART}("@staticvar")  	{setNewTag(PHPDocTagKinds.STATICVAR);}
<ST_IN_TAGS>^{LINESTART}("@subpackage") 	{setNewTag(PHPDocTagKinds.SUBPACKAGE);}
<ST_IN_TAGS>^{LINESTART}("@throws")     	{setNewTag(PHPDocTagKinds.THROWS);}
<ST_IN_TAGS>^{LINESTART}("@todo")       	{setNewTag(PHPDocTagKinds.TODO);}
<ST_IN_TAGS>^{LINESTART}("@tutorial")   	{setNewTag(PHPDocTagKinds.TUTORIAL);}
<ST_IN_TAGS>^{LINESTART}("@uses")       	{setNewTag(PHPDocTagKinds.USES);}
<ST_IN_TAGS>^{LINESTART}("@property")   	{setNewTag(PHPDocTagKinds.PROPERTY);}
<ST_IN_TAGS>^{LINESTART}("@property-read")  {setNewTag(PHPDocTagKinds.PROPERTY_READ);}
<ST_IN_TAGS>^{LINESTART}("@property-write") {setNewTag(PHPDocTagKinds.PROPERTY_WRITE);}
<ST_IN_TAGS>^{LINESTART}("@var")        	{setNewTag(PHPDocTagKinds.VAR);}
<ST_IN_TAGS>^{LINESTART}("@version")    	{setNewTag(PHPDocTagKinds.VERSION);}

<ST_IN_TAGS>{TABS_AND_SPACES}("*/") {handleDocEnd_inTags();return -1;}

<ST_IN_TAGS>{NEWLINE}     {appendText();}

<ST_IN_TAGS>.             {}
