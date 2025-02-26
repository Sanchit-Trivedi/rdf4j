/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.spin.function;

import org.eclipse.rdf4j.common.exception.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.function.TupleFunction;
import org.eclipse.rdf4j.query.algebra.evaluation.function.TupleFunctionRegistry;

public class KnownTupleFunctionParser implements TupleFunctionParser {

	private final TupleFunctionRegistry functionRegistry;

	public KnownTupleFunctionParser(TupleFunctionRegistry functionRegistry) {
		this.functionRegistry = functionRegistry;
	}

	@Override
	public TupleFunction parse(IRI funcUri, TripleSource store) throws RDF4JException {
		return functionRegistry.get(funcUri.stringValue()).orElse(null);
	}
}
