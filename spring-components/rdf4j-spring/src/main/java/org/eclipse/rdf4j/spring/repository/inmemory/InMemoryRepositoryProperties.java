/*******************************************************************************
 * Copyright (c) 2021 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/

package org.eclipse.rdf4j.spring.repository.inmemory;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @since 4.0.0
 * @author Gabriel Pickl
 * @author Florian Kleedorfer
 */
@ConfigurationProperties(prefix = "rdf4j.spring.repository.inmemory")
public class InMemoryRepositoryProperties {
	private boolean enabled = true;
	/** Should a SHACL Sail be used? */
	private boolean useShaclSail = false;

	public boolean isUseShaclSail() {
		return useShaclSail;
	}

	public void setUseShaclSail(boolean useShaclSail) {
		this.useShaclSail = useShaclSail;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
