//### CONSTANTS ###
var XMLNS_XSD="http://www.w3.org/2001/XMLSchema";
var XMLNS_XSI="http://www.w3.org/2001/XMLSchema-instance";
var XMLNS_SOAPENC="http://schemas.xmlsoap.org/soap/encoding/"
var XMLNS_SOAPENV="http://schemas.xmlsoap.org/soap/envelope/";
var XMLNS_PREFIX_MAP=new Array();
XMLNS_PREFIX_MAP[XMLNS_XSD]="xsd";
XMLNS_PREFIX_MAP[XMLNS_XSI]="xsi";
XMLNS_PREFIX_MAP[XMLNS_SOAPENC]="soapenc";
XMLNS_PREFIX_MAP[XMLNS_SOAPENV]="soapenv";


//*********************************
//QName(localpart)
//QName(namespaceUri,localpart)
//QName(namespaceUri,localpart,prefix)
//*********************************
function QName() {
	this.namespaceUri=null;
	this.localpart=null;
	this.prefix=null;
	this.init(arguments);
}

QName.prototype.init=function(args){
	if(args.length==1) {
		this.localpart=args[0];
	}else if(args.length==2) {
		this.namespaceUri=args[0];
		this.localpart=args[1];	
	}else if(args.length==3) {
		this.namespaceUri=args[0];
		this.localpart=args[1];
		this.prefix=args[2];
	}
}

QName.prototype.hashKey=function() {
	return this.namespaceUri+"#"+this.localpart;
}

QName.prototype.toString=function() {
	var name=this.localpart;
	if(this.prefix!=null) name=this.prefix+":"+name;
	if(this.namespaceUri!=null) {
		name+=" xmlns";
		if(this.prefix!=null) name=name+":"+this.prefix;
		name=name+"=\""+this.namespaceUri+"\"";
	}
	return name; 
}

//### CONSTANTS ###
var QNAME_XSI_TYPE=new QName(XMLNS_XSI,"type");


//*********************************
//XMLType
//*********************************
function XMLType() {

	this.XSD_BASE64=new QName(XMLNS_XSD,"base64Binary");
	this.XSD_BOOLEAN=new QName(XMLNS_XSD,"boolean");
	this.XSD_BYTE=new QName(XMLNS_XSD,"byte");
	this.XSD_DATETIME=new QName(XMLNS_XSD,"dateTime");
	this.XSD_DECIMAL=new QName(XMLNS_XSD,"decimal");
	this.XSD_DOUBLE=new QName(XMLNS_XSD,"double");
	this.XSD_FLOAT=new QName(XMLNS_XSD,"float");
	this.XSD_HEXBINARY=new QName(XMLNS_XSD,"hexBinary");
	this.XSD_INT=new QName(XMLNS_XSD,"int");
	this.XSD_INTEGER=new QName(XMLNS_XSD,"integer");
	this.XSD_LONG=new QName(XMLNS_XSD,"long");
	this.XSD_QNAME=new QName(XMLNS_XSD,"QName");
	this.XSD_SHORT=new QName(XMLNS_XSD,"short");
	this.XSD_STRING=new QName(XMLNS_XSD,"string");
	
	this.SOAP_ARRAY=new QName(XMLNS_SOAPENC,"Array");
	this.SOAP_BASE64=new QName(XMLNS_SOAPENC,"base64");
	this.SOAP_BOOLEAN=new QName(XMLNS_SOAPENC,"boolean");
	this.SOAP_BYTE=new QName(XMLNS_SOAPENC,"byte");
	this.SOAP_DOUBLE=new QName(XMLNS_SOAPENC,"double");
	this.SOAP_FLOAT=new QName(XMLNS_SOAPENC,"float");
	this.SOAP_INT=new QName(XMLNS_SOAPENC,"int");
	this.SOAP_LONG=new QName(XMLNS_SOAPENC,"long");
	this.SOAP_SHORT=new QName(XMLNS_SOAPENC,"short");
	this.SOAP_STRING=new QName(XMLNS_SOAPENC,"string");
	
}

var xmltype=new XMLType();

//*********************************
//Parameter(name,xmlType,parameterMode)
//Parameter(name,xmlType,jsType,parameterMode)
//*********************************
function Parameter() {
	this.name=null;
	this.xmlType=null;
	this.jsType=null;
	this.parameterMode=null;
	this.init(arguments);
	this.value=null;
}

Parameter.prototype.init=function(args) {
	if(args.length==3) {
		this.name=args[0];
		this.xmlType=args[1];
		this.parameterMode=args[2];
	} else if(args.length==4) {
		this.name=args[0];
		this.xmlType=args[1];
		this.jsType=args[2];
		this.parameterMode=args[3];
	}
}

Parameter.prototype.setValue=function(value) {
	this.value=value;
}


//*********************************
//XMLContext(XMLContext parent)
//*********************************
function XMLContext(parent) {
	this.parent=parent
	this.prefixMap=new Array();
}

//String getPrefix(String nsuri)
XMLContext.prototype.getPrefix=function(nsuri) {
	var prefix=this.prefixMap[nsuri];
	if(prefix==null && this.parent!=null) {
		prefix=this.parent.getPrefix(nsuri);
	}
	return prefix;
}

//setPrefix(String nsuri,String prefix)
XMLContext.prototype.setPrefix=function(nsuri,prefix) {
	this.prefixMap[nsuri]=prefix;
}


//*********************************
//XMLWriter()
//*********************************
function XMLWriter() {
	this.xml=null;
	this.currentCtx=null;
	this.init();
	this.inStart=false;
	this.prefixMap=new Array();
	this.prefixBase="ns";
	this.prefixCnt=0;
}

XMLWriter.prototype.init=function() {
	this.xml="";
}

//startElement(String name)
//startElement(QName name)
XMLWriter.prototype.startElement=function(name) {
	this.currentCtx=new XMLContext(this.currentCtx);
	if(this.inStart) {
		this.xml+=">";
	}
	var tagName=null;
	var prefix=null;
	var nsuri=null;
	var newNS=false;
	if(name instanceof QName) {
		tagName=name.localpart;
		nsuri=name.namespaceUri;
		if(nsuri!=null) {
			prefix=this.currentCtx.getPrefix(nsuri);
			if(prefix==null) {
				prefix=this.getPrefix(nsuri);
				newNS=true;
			}
		}
	} else {
		tagName=name;
	}
	if(prefix!=null) tagName=prefix+":"+tagName;
	this.xml+="<"+tagName;
	if(newNS) this.writeNamespaceDeclaration(nsuri,prefix);
	this.inStart=true;
}

//endElement(String name)
//endElement(QName name)
XMLWriter.prototype.endElement=function(name) {
	if(this.inStart) {
		this.xml+="/>";
	} else {
		var tagName=null;
		var prefix=null;
		var nsuri=null;
		if(name instanceof QName) {
			tagName=name.localpart;
			nsuri=name.namespaceUri;
			if(nsuri!=null) {
				prefix=this.currentCtx.getPrefix(nsuri);
			}
		} else {
			tagName=name;
		}
		if(prefix!=null) tagName=prefix+":"+tagName;
		this.xml+="</"+tagName+">";
	}
	this.inStart=false;
	this.currentCtx=this.currentCtx.parent;
}

//writeAttribute(String name,String value)
//writeAttribute(QName name,String value)
XMLWriter.prototype.writeAttribute=function(name,value) {
	var attrName=null;
	var prefix=null;
	var nsuri=null;
	var newNS=false;
	if(name instanceof QName) {
		attrName=name.localpart;
		nsuri=name.namespaceUri;
		if(nsuri!=null) {
			prefix=this.currentCtx.getPrefix(nsuri);
			if(prefix==null) {
				prefix=this.getPrefix(nsuri);
				newNS=true;
			}
		}
	} else {
		attrName=name;
	}
	if(prefix!=null) attrName=prefix+":"+attrName;
	if(newNS) this.writeNamespaceDeclaration(nsuri,prefix);
	this.xml+=" "+attrName+"=\""+value+"\"";
}

//writeChars(String chars)
XMLWriter.prototype.writeChars=function(chars) {
	if(this.inStart) {
		this.xml+=">";
		this.inStart=false;
	}
	this.xml+=chars;
}

//writeNamespaceDeclaration(String nsuri)
//writeNamespaceDeclaration(String nsuri,String prefix)
XMLWriter.prototype.writeNamespaceDeclaration=function() {
	if(arguments.length==1) {
		var prefix=this.getPrefix(arguments[0]);
		this.writeNamespaceDeclaration(arguments[0],prefix);
	} else if(arguments.length==2) {
		this.xml+=" xmlns:"+arguments[1]+"=\""+arguments[0]+"\"";
		this.currentCtx.setPrefix(arguments[0],arguments[1]);
	}
}

//String getPrefix(String nsuri)
XMLWriter.prototype.getPrefix=function(nsuri) {
	var prefix=this.prefixMap[nsuri];
	if(prefix==null) {
		prefix=XMLNS_PREFIX_MAP[nsuri];
		if(prefix==null) {
			prefix=this.prefixBase+this.prefixCnt;
			this.prefixCnt++;
		}
	}
	this.prefixMap[nsuri]=prefix;
	return prefix;
}


//*********************************
//SimpleTypeSerializer(type)
//*********************************
function SimpleTypeSerializer(type) {
	this.type=null;
	this.init(arguments);
}

SimpleTypeSerializer.prototype.init=function(args) {
	if(args.length==1) {
		this.type=args[0];
	} else throw "Wrong argument number";
}

//serialize(value,name,writer)
SimpleTypeSerializer.prototype.serialize=function(value,name,writer) {
	writer.startElement(name);
	var prefix=writer.getPrefix(this.type.namespaceUri);
	writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+this.type.localpart);
	writer.writeChars(value);
	writer.endElement(name);
}


//*********************************
//ArraySerializer(type)
//*********************************
function ArraySerializer() {
	this.type=xmltype.SOAP_ARRAY;
}

ArraySerializer.prototype.serialize=function(value,name,writer) {
	writer.startElement(name);
	var prefix=writer.getPrefix(this.type.namespaceUri);
	writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+this.type.localpart);
	for(var i=0;i<value.length;i++) {
		writer.startElement("item");
		
		/*
		alert(typeof value[i]);
		if( typeof value[i] == "number" ) {
			if( (value[i]-Math.floor(value[i]))>0 ) {
				// float
			} else {
				// int
				var prefix=writer.getPrefix(xmltype.XSD_INT.namespaceUri);
				writer.writeAttribute(QNAME_XSI_TYPE,prefix+":"+xmltype.XSD_INT.localpart);
			}
		} else if( typeof value[i] == "string" ) {
			
		}
		*/
		
		writer.writeChars(value[i]);
		writer.endElement("item");
	}
	writer.endElement(name);
}


//*********************************
//TypeMapping()
//*********************************
function TypeMapping() {
	this.mappings=new Array();
	this.builtin=new Array();
	this.SER_SIMPLE=1;
	this.SER_ARRAY=2;
	this.initBuiltin();
}

//initBuiltin()
TypeMapping.prototype.initBuiltin=function() {
	this.builtin[xmltype.XSD_INT.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltype.XSD_STRING.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltype.SOAP_ARRAY.hashKey()]=this.SER_ARRAY;
	this.builtin[xmltype.SOAP_INT.hashKey()]=this.SER_SIMPLE;
	this.builtin[xmltype.SOAP_STRING.hashKey()]=this.SER_SIMPLE;
}

//register(QName type,Serializer serializer)
TypeMapping.prototype.register=function(type,serializer) {
	this.mappings[type.hashKey()]=serializer;
}

//Serializer getSerializer(QName type) 
TypeMapping.prototype.getSerializer=function(type) {
	var serializer=this.mappings[type.hashKey()];
	if(serializer==null) serializer=this.getBuiltinSerializer(type);
	if(serializer!=null) this.register(type,serializer);
	else throw "Can't find serializer for type '"+type.toString()+"'";
	return serializer;
}

//Serializer getBuiltinSerializer(QName type)
TypeMapping.prototype.getBuiltinSerializer=function(type) {
	var serType=this.builtin[type.hashKey()];
	if(serType==this.SER_SIMPLE) {
		return new SimpleTypeSerializer(type);
	} else if(serType==this.SER_ARRAY) {
		return new ArraySerializer();
	}
}

var typeMapping=new TypeMapping();


//*********************************
// RPCSerializer(QName opName,ArrayOfParameter params,values,...)
//*********************************
function RPCSerializer(opName,params) {
	this.opName=opName;
	this.params=params;
}

RPCSerializer.prototype.serialize=function(writer) {
	writer.startElement(this.opName);
	var ser=new SimpleTypeSerializer(xmltype.XSD_INT);
	for(var i=0;i<this.params.length;i++) {
		var serializer=typeMapping.getSerializer(this.params[i].xmlType);
		serializer.serialize(this.params[i].value,this.params[i].name,writer);
	}
	writer.endElement(this.opName);
}


//*********************************
// Call()
//*********************************
function Call() {
	this.endpoint=null;
	this.opName=null;
	this.params=new Array();
	this.retXmlType=null;
	this.retJsType=null;
  this.callback=null;
}

//setTargetEndpointAddress(address)
Call.prototype.setTargetEndpointAddress=function() {
	if(arguments.length==1) {
		this.endpoint=arguments[0];
	}
}

//setOperationName(operationName)
Call.prototype.setOperationName=function() {
	if(arguments.length==1) {
		this.opName=arguments[0];
	}
}	

//addParameter(paramName,xmlType,parameterMode)
//addParameter(paramName,xmlType,jsType,parameterMode)
Call.prototype.addParameter=function() {
	var param;
	if(arguments.length==3) {
		param=new Parameter(arguments[0],arguments[1],arguments[2]);
	} else if(arguments.length==4) {
		param=new Parameter(arguments[0],arguments[1],arguments[2],arguments[3]);
	}
	if(param!=null) this.params.push(param);
}

//setReturnType(xmlType)
//setReturnType(xmlType,jsType)
Call.prototype.setReturnType=function() {
	if(arguments.length==1) {
		this.retXmlType=arguments[0];
	} else if(arguments.length==2) {
		this.retXmlType=arguments[0];
		this.retJsType=arguments[1];
	}
}

//invoke()
//invoke(values,...)
//invoke(QName operationName,values,...)
Call.prototype.invoke=function() {
	var writer=new XMLWriter();
	var soapMsg=new SOAPMessage();
	
	var ind=0;
	if(arguments.length>0) {
		if(arguments[0] instanceof QName) {
			this.setOperationName(arguments[0]);
			ind++;
		}
	}
	if(this.params.length!=arguments.length-ind) throw "Wrong number of arguments";
	for(var i=0;i<this.params.length;i++) {
		this.params[i].setValue(arguments[i+ind]);
	}
	var rpc=new RPCSerializer(this.opName,this.params);
	
	var bodyElem=new SOAPBodyElement(rpc);
	soapMsg.getSOAPPart().getEnvelope().getBody().addBodyElement(bodyElem);
	soapMsg.write(writer);
  //  alert("writer.xml:\n" + writer.xml);
  	document.getElementById('request').value=writer.xml;

  return new xmlRequest( 'POST', this.endpoint, this.callback ).start( writer.xml );
}




//*********************************
// SOAPMessage()
//*********************************
function SOAPMessage() {
	this.soapPart=new SOAPPart();
}

//SOAPPart getSOAPPart()
SOAPMessage.prototype.getSOAPPart=function() {
	return this.soapPart;
}

//write(XMLWriter writer) {
SOAPMessage.prototype.write=function(writer) {
	this.soapPart.write(writer);
}

//*********************************
// SOAPPart()
//*********************************
function SOAPPart() {
	this.envelope=new SOAPEnvelope();
	
}

//SOAPEnvelope getEnvelope()
SOAPPart.prototype.getEnvelope=function() {
	return this.envelope;
}

//write(XMLWriter writer) {
SOAPPart.prototype.write=function(writer) {
	this.envelope.write(writer);
}


//*********************************
// SOAPEnvelope()
//*********************************
function SOAPEnvelope() {
	this.header=new SOAPHeader();
	this.body=new SOAPBody();
}

//write(XMLWriter writer) {
SOAPEnvelope.prototype.write=function(writer) {
	var envName=new QName(XMLNS_SOAPENV,"Envelope");
	writer.startElement(envName);
	writer.writeNamespaceDeclaration(XMLNS_XSI);
	writer.writeNamespaceDeclaration(XMLNS_XSD);
	this.body.write(writer);
	writer.endElement(envName);
}

//SOAPHeader getHeader()
SOAPEnvelope.prototype.getHeader=function() {
	return this.header;
}

//SOAPBody getBody()
SOAPEnvelope.prototype.getBody=function() {
	return this.body;
}

//*********************************
// SOAPBodyElement(serializer)
//*********************************
function SOAPBodyElement(serializer) {
	this.serializer=serializer;
}

SOAPBodyElement.prototype.write=function(writer) {
	this.serializer.serialize(writer);
}

//*********************************
// SOAPBody()
//*********************************
function SOAPBody() {
	this.fault=null;
	this.bodyElems=new Array();
}

//addBodyElement(SOAPBodyElement bodyElem)
SOAPBody.prototype.addBodyElement=function(bodyElem) {
	this.bodyElems.push(bodyElem);	
}

//setFault(SOAPFault fault) 
SOAPBody.prototype.setFault=function(fault) {
	this.fault=fault;
}

//write(XMLWriter writer) {
SOAPBody.prototype.write=function(writer) {
	var bodyName=new QName(XMLNS_SOAPENV,"Body");
	writer.startElement(bodyName);
	if(this.fault!=null) {
		return;
	}
	if(this.bodyElems!=null) {
		for(var i=0;i<this.bodyElems.length;i++) {
			this.bodyElems[i].write(writer);
		}
	}
	writer.endElement(bodyName);
}

//*********************************
// SOAPHeaderElement(serializer)
//*********************************
function SOAPHeaderElement(serializer) {
	this.serializer=serializer;
}

SOAPHeaderElement.prototype.write=function(writer) {
	this.serializer.serialize(writer);
}

//*********************************
// SOAPHeader()
//*********************************
function SOAPHeader() {
	this.headerElems=new Array();
}

SOAPHeader.prototype.addHeaderElement=function(headerElem) {
	this.headerElems.push(headerElem);
}

//write(XMLWriter writer) {
SOAPHeader.prototype.write=function(writer) {
	var headerName=new QName(XMLNS_SOAPENV,"Header");
	writer.startElement(headerName);
	for(var i=0;i<this.headerElems.length;i++) {
		this.headerElems[i].write(writer);
	}
	writer.endElement(headerName);
}

//*********************************
// SOAPFault(String faultCode,String faultString)
//*********************************
function SOAPFault(faultCode,faultString) {
	this.faultCode=faultCode;
	this.faultString=faultString;
}


function test() {
	
	var call=new Call();
	call.setTargetEndpointAddress(window.location.protocol + "//" + window.location.host + "/xml/webservice/Calculator");
	call.setOperationName(new QName("add"));
	call.addParameter("value1",xmltype.XSD_INT,"IN");
	//call.addParameter("value2",xmltype.XSD_INT,"IN");
	//call.addParameter("test",xmltype.SOAP_STRING,"IN");
	call.addParameter("test",xmltype.SOAP_ARRAY,"IN");
	call.setReturnType(xmltype.XSD_INT);
	//try {
		call.invoke(4,new Array(4,"jj","fdfdfd"));
	//} catch(exception) {
	//	alert(exception);
	//}
	
}

