/*******************************************************************************
 * Copyright (c) 2021 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.lmdb;

import java.io.Serializable;

import org.eclipse.rdf4j.sail.lmdb.model.LmdbValue;

/**
 * A {@link ValueStore ValueStore} revision for {@link LmdbValue LmdbValue} objects. For a cached value ID of a
 * LmdbValue to be valid, the revision object needs to be equal to the concerning ValueStore's revision object. The
 * ValueStore's revision object is changed whenever values are removed from it or IDs are changed.
 *
 *
 */
public class ValueStoreRevision implements Serializable {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -2434063125560285009L;

	/*-----------*
	 * Variables *
	 *-----------*/

	transient private final ValueStore valueStore;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueStoreRevision(ValueStore valueStore) {
		this.valueStore = valueStore;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueStore getValueStore() {
		return valueStore;
	}

	public boolean resolveValue(long id, LmdbValue value) {
		return valueStore.resolveValue(id, value);
	}
}
