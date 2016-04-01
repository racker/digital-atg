package com.rax.deployment;

/**
	#################### COPYRIGHT #####################################
	
	Copyright 2001, 2015, Rackspace Inc and/or its affiliates. All rights reserved.
	UNIX is a registered trademark of The Open Group.
	This software and related documentation are provided under a license
	agreement containing restrictions on use and disclosure and are
	protected by intellectual property laws. Except as expressly permitted
	in your license agreement or allowed by law, you may not use, copy,
	reproduce, translate, broadcast, modify, license, transmit, distribute,
	exhibit, perform, publish, or display any part, in any form, or by any
	means. Reverse engineering, disassembly, or decompilation of this
	software, unless required by law for interoperability, is prohibited.
	The information contained herein is subject to change without notice
	and is not warranted to be error-free. If you find any errors, please
	report them to us in writing.

	This software or hardware and documentation may provide access to or
	information on content, products, and services from third parties.
	Rackspace Inc, and its affiliates are not responsible for and
	expressly disclaim all warranties of any kind with respect to
	third-party content, products, and services. Rackspace Inc. and
	its affiliates will not be responsible for any loss, costs, or damages
 	incurred due to your access to or use of third-party content, products,
   or services.
 **/

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import atg.rest.client.RestClientException;
import atg.rest.client.RestComponentHelper;
import atg.rest.client.RestResult;
import atg.rest.client.RestSession;
import atg.service.dynamo.DAFConfiguration;

/**
 * This class is used to update the Deployment Topology by making certain REST
 * calls to the BCC server. 
 * 
 * The first call is to get the current topology details, and then to compare if 
 * this current host is added to the topology definition. Only if the topology does
 * not contain the host information, another REST call is made to update the topology. 
 * 
 * @author dev
 *
 */

public class TopologyUpdateClient extends GenericService {

	// host to call to initiate RestSession
	private String mHost;

	public String getHost() {
		return mHost;
	}

	public void setHost(String pHost) {
		this.mHost = pHost;
	}
	

	// port to call to initiate RestSession
	private int mPort;

	public int getPort() {
		return mPort;
	}

	public void setPort(int pPort) {
		this.mPort = pPort;
	}
	// Only run service if enabled is set to true
	private boolean mEnabled = false;

	public boolean isEnabled() {
		return mEnabled;
	}

	public void setEnabled(boolean pEnabled) {
		this.mEnabled = pEnabled;
	}
	
	// Should point to Configuration.thisHost 
	DAFConfiguration mConfiguration;

	public DAFConfiguration getConfiguration() {
		return mConfiguration;
	}

	public void setConfiguration(DAFConfiguration pConfiguration) {
		this.mConfiguration = pConfiguration;
	}
	
	// Some of the REST URIs that may be called 
	String mRestHost;
	String mInfoTopologyPath;
	String mUpdateTopologyPath;
	String mDeleteUpdatePath;
	String mInitUpdatePath;
	
	public String getUpdateTopologyPath() {
		return mUpdateTopologyPath;
	}

	public void setUpdateTopologyPath(String pUpdateTopologyPath) {
		this.mUpdateTopologyPath = pUpdateTopologyPath;
	}

	public String getRestHost() {
		return mRestHost;
	}

	public void setRestHost(String pRestHost) {
		this.mRestHost = pRestHost;
	}

	public String getInfoTopologyPath() {
		return mInfoTopologyPath;
	}

	public void setInfoTopologyPath(String pInfoTopologyPath) {
		this.mInfoTopologyPath = pInfoTopologyPath;
	}

	public String getDeleteUpdatePath() {
		return mDeleteUpdatePath;
	}

	public void setDeleteUpdatePath(String pDeleteUpdatePath) {
		this.mDeleteUpdatePath = pDeleteUpdatePath;
	}

	public String getInitUpdatePath() {
		return mInitUpdatePath;
	}

	public void setInitUpdatePath(String pInitUpdatePath) {
		this.mInitUpdatePath = pInitUpdatePath;
	}

	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		if (isEnabled()) {
			try {
				initializeTopology();
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				
			}
		}
	}

	public void initializeTopology() throws RestClientException {
		RestSession session = null;
		
		try {
			session = RestSession.createSession(getHost(), getPort());
			if (session != null) {
				session.startSession();
				try {
					String topologyXML = getTopologyInfo(session);
					try {
						if (!isRmiURIinXML (topologyXML)) {
						}
					} catch (Exception e) {
						//TODO
					}
				} catch (IOException e) {
					//TODO
				} 
			}
			
		} finally {
			if (session != null) {
				session = null;
			}
		}
	}
	
	private boolean isRmiURIinXML(String topologyXML) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		
		ByteArrayInputStream input = new ByteArrayInputStream (topologyXML.getBytes("UTF-8"));
		Document doc = builder.parse(input);
		// doc.getDocumentElement().normalize();
		XPath xPath = XPathFactory.newInstance().newXPath();
		String rmiuri = getConfiguration().getThisHostname() + ":" + getConfiguration().getRmiPort();
		String expression = "/publishing-deployment-topology/target/agent/transport/rmi-uri";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc,XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength() ; i++) {
			String val = nodeList.item(i).getFirstChild().getNodeValue();
			vlogDebug ("The node uri obtained: " + val);
			if (val.contains(rmiuri)) {
				vlogInfo("Agent already added for " + rmiuri);
				return true;
			}
		}
		
		return false;
		
	}

	// Return the xml representation within the String on the 
	// deployment topology
	private String getTopologyInfo (RestSession session) throws RestClientException, IOException {
		RestResult result = null;
		try {
			if (session != null) {
				result = RestComponentHelper.getComponent(getRestHost() + getInfoTopologyPath() , null, session);
				String responseData = result.readInputStream();
				if (responseData != null) {
					try {
						JSONObject json = new JSONObject (responseData);
						vlogDebug(json.toString());
						return formatXML (json.getString("result"));
					}catch (JSONException e) {
						vlogError ("Not a valid json response obtained",e);
					}
				}
			} 
		} finally {
			if (result != null) {
				result.close();
			}
		}
		return null;
	}
	
	// Format the json response - remove escape characters as a result of how the
	// data is being transmitted over the wire
	private String formatXML (String data) {
		if (StringUtils.isNotEmpty(data)) {
			data.replaceAll("\\\"", "\"").replaceAll("\\n", "").replaceAll("<\\/", "</");
			vlogDebug (data);
		}
		return data;
	}
	
}
