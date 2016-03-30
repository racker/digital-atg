package com.rax.deployment;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import atg.core.util.StringUtils;
import atg.nucleus.GenericService;
import atg.nucleus.ServiceException;
import atg.rest.client.RestClientException;
import atg.rest.client.RestComponentHelper;
import atg.rest.client.RestResult;
import atg.rest.client.RestSession;

public class DeploymentClient extends GenericService {

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
	String mThisHost;
	
	
	public String getThisHost() {
		return mThisHost;
	}

	public void setThisHost(String pThisHost) {
		this.mThisHost = pThisHost;
	}

	@Override
	public void doStartService() throws ServiceException {
		super.doStartService();
		if (isEnabled()) {
			try {
				initializeTopology();
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					List <String> rmiURI = rmiURIFromXML (topologyXML);
				} catch (IOException e) {
					
				} 
			}
			
		} finally {
			if (session != null) {
				session = null;
			}
		}
	}
	
	private List<String> rmiURIFromXML(String topologyXML) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbFactory.newDocumentBuilder();
			
			ByteArrayInputStream input = new ByteArrayInputStream (topologyXML.getBytes("UTF-8"));
			Document doc = builder.parse(input);
			// doc.getDocumentElement().normalize();
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "/publishing-deployment-topology/target/agent/transport/rmi-uri";
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc,XPathConstants.NODESET);
			List <String> uriList = new ArrayList <String> (nodeList.getLength());
			for (int i = 0; i < nodeList.getLength() ; i++) {
				uriList.add(nodeList.item(i).getFirstChild().getNodeValue());
			}
			return uriList;
			
		} catch (Exception e) {
			
		}
		return null;
		
	}

	// Return the xml representation within the String on the 
	// deployment topology
	private String getTopologyInfo (RestSession session) throws RestClientException, IOException {
		RestResult result = null;
		try {
			if (session != null) {
				result = RestComponentHelper.getComponent("http://52.2.31.4:20180/rest/model/rax/deployment/TopologyActor/info", null, session);
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
