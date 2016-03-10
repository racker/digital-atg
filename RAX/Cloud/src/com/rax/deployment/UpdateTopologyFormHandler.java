/**
 	#################### COPYRIGHT #####################################
    Copyright 2001, 2015, Rackspace Inc and/or its affiliates. All rights 
    reserved.
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
 */

package com.rax.deployment;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import atg.core.util.StringUtils;
import atg.deployment.common.DeploymentException;
import atg.deployment.server.topology.AgentDef;
import atg.deployment.server.topology.TargetDef;
import atg.deployment.server.topology.TopologyDef;

import atg.deployment.server.ui.TopologyEditFormHandler;
import atg.servlet.DynamoHttpServletRequest;
import atg.servlet.DynamoHttpServletResponse;

/**
 * Formhandler provides method to add agent to surrogate via remote means
 * 
 * @author dev
 * 
 */
public class UpdateTopologyFormHandler extends TopologyEditFormHandler {

	// Target name to get the appropriate target ID to update

	String mTargetName;

	public String getTargetName() {
		return mTargetName;
	}

	public void setTargetName(String pTargetName) {
		this.mTargetName = pTargetName;
	}

	/**
	 * This method provides an alternate route to to facilitate topology update
	 * outside of the BCC
	 * 
	 * It sets up required variables, before calling the handleAddAgent
	 * 
	 * @param pRequest
	 * @param pResponse
	 * @return
	 * @throws DeploymentException
	 * @throws IOException
	 * @throws ServletException
	 */
	public boolean handleAddAgentByTarget(DynamoHttpServletRequest pRequest,
			DynamoHttpServletResponse pResponse) throws DeploymentException,
			IOException, ServletException {

		vlogDebug("UpdateTopologyFormHandler: handleAddAgentViaRemote called");
		setTargetID(findTarget(getTargetName()).getID());
		setAgentEssential(Boolean.FALSE);
		String[] assetDestinations = { "/atg/epub/file/WWWFileSystem",
				"/atg/epub/file/ConfigFileSystem" };
		setIncludeAssetDestinations(assetDestinations);
		return super.handleAddAgent(pRequest, pResponse);
	}

	
	
	protected void preDeleteAgent (DynamoHttpServletRequest pRequest,DynamoHttpServletResponse pResponse) 
	  throws IOException, ServletException {
		if (StringUtils.isEmpty(getAgentID())) {
			try {
			  if (StringUtils.isNotEmpty(getTargetName()) && StringUtils.isNotEmpty(getAgentDisplayName())) {
			    AgentDef agent = getAgentByName (findTarget(getTargetName().trim()),getAgentDisplayName().trim());
			    if (agent != null) {
			      vlogDebug(" Agent to be deleted with ID: " + agent.getID());
				  setAgentID(agent.getID());
			    }
			  }
			} catch (DeploymentException e) {
				processException (null,"errorGeneral",pRequest,pResponse);
			}
			
		}
		 super.preDeleteAgent(pRequest, pResponse);
	}
	
	// Return the Target based on the display name
    @SuppressWarnings("unchecked")
	private TargetDef findTarget (String pTargetName) throws DeploymentException {
    	
    	if (!StringUtils.isEmpty(pTargetName)) {
    	  vlogDebug("Target name to obtain :" + pTargetName);
    	  TopologyDef primary = getDeploymentServer().getTopologyManager().getSurrogateTopology();
	      if (primary != null) {
	      
		    List <TargetDef> targets = primary.getTargets();
	    	Iterator <TargetDef> it = targets.iterator();
	    	while (it.hasNext()) {
	    	  TargetDef target = it.next();
	    	  if (pTargetName.equals (target.getDisplayName())) {
	    		vlogDebug("Obtained target name with target id " + target.getID());
	    	    return target;
	    	  }	
	    	}
	      }
    	}	
    	vlogDebug("Could not obtain target for targetName  " + pTargetName);
	    
    	return null;
    }
    
    @SuppressWarnings("unchecked")
	private AgentDef getAgentByName (TargetDef pTarget, String pAgentName) throws DeploymentException {
    	if (pTarget != null && !StringUtils.isEmpty(pAgentName)) {
          vlogDebug("Agent Name to obtain: " + pAgentName);		
    	  Collection<AgentDef> agents = pTarget.getAgents();
    	  Iterator <AgentDef> it = agents.iterator();
    	  while (it.hasNext()) {
    		  AgentDef ag = it.next();
    		  if (pAgentName.equals (ag.getDisplayName())) {
    			vlogDebug("Found agent name with id :" + ag.getID());
  	    	    return ag;
  	    	  }
    	  }
      }
      vlogDebug("Could not find agent name for agent :" + pAgentName);
  	    
      return null;
    }

}
