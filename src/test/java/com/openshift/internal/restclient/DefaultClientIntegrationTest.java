 /*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package com.openshift.internal.restclient;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openshift.internal.restclient.ResourceFactory;
import com.openshift.internal.restclient.model.Project;
import com.openshift.internal.restclient.model.Service;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IResourceFactory;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IResource;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.template.ITemplate;

/**
 * @author Jeff Cantrill
 */
public class DefaultClientIntegrationTest {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultClientIntegrationTest.class);
	
	private IClient client;
	private IntegrationTestHelper helper = new IntegrationTestHelper();

	@Before
	public void setup () {
		client = helper.createClient();
	}
	
	@Test
	public void testListTemplates(){
		List<ITemplate> list = client.list(ResourceKind.Template, "test");
		for (ITemplate template : list) {
			LOG.debug(template.toString());
		}
	}
	
	@Test
	public void testResourceLifeCycle() throws MalformedURLException {
		
		IResourceFactory factory = new ResourceFactory(client);
		
		IProject project = factory.create("v1beta1", ResourceKind.Project);
		project.setName("firstproject");
		LOG.debug(String.format("Stubbing project: %s", project));
		
		IProject other = factory.create("v1beta1", ResourceKind.Project);
		other.setName("other");
		LOG.debug(String.format("Stubbing project: %s", project));
		
		IService service = factory.create("v1beta1", ResourceKind.Service);
		service.setNamespace(project.getName()); //this will be the project's namespace
		service.setName("some-service");
		service.setContainerPort(6767);
		service.setPort(6767);
		service.setSelector("name", "barpod");
		LOG.debug(String.format("Stubbing service: %s", service));

		Service otherService = factory.create("v1beta1", ResourceKind.Service);
		otherService.setNamespace("someothernamespace"); //this will be the project's namespace
		otherService.setName("some-other-service");
		otherService.setContainerPort(8787);
		otherService.setPort(8787);
		otherService.setSelector("name", "foopod");
		
		LOG.debug(String.format("Stubbing service: %s", otherService));
		
		try{
			project = client.create(project);
			LOG.debug(String.format("Created project: %s", project));

			other = client.create(other);
			LOG.debug(String.format("Created project: %s", project));
			
			LOG.debug(String.format("Creating service: %s", service));
			service = client.create(service);
			LOG.debug(String.format("Created service: %s", service));
			
			LOG.debug(String.format("Creating service: %s", otherService));
			otherService = client.create(otherService);
			LOG.debug(String.format("Created service: %s", otherService));
			
			LOG.debug(String.format("Listing projects with namespace: %s", project.getNamespace()));
			List<Project> projects = client.list(ResourceKind.Project, project.getNamespace());
			LOG.debug(String.format("Listed projects: %s", projects));
			
			assertEquals("Expected to get the project with the correct namespace", project.getName(), projects.get(0).getName());
			
			LOG.debug(String.format("Listing services with namespace: %s", project.getNamespace()));
			List<Service> services = client.list(ResourceKind.Service, project.getNamespace());
			LOG.debug(String.format("Listed services: %s", services));
			
			LOG.debug(String.format("Getting service: %s", otherService.getName()));
			Service s = client.get(ResourceKind.Service, otherService.getName(), otherService.getNamespace());
			LOG.debug(String.format("Retrieved service: %s", s.getName()));
			
			assertEquals("Expected there to be only one service returned", 1, services.size());
			assertEquals("Expected to get the service with the correct name", service.getName(), services.get(0).getName());
		}finally{
			cleanUpResource(client, project);
			cleanUpResource(client, other);
			cleanUpResource(client, service);
			cleanUpResource(client, otherService);
		}
		
	}

	private void cleanUpResource(IClient client, IResource resource){
		try{
			Thread.sleep(1000);
			LOG.debug(String.format("Deleting resource: %s", resource));
			client.delete(resource);
		}catch(Exception e){
			LOG.error("Exception deleting", e);
		}
	}

}
