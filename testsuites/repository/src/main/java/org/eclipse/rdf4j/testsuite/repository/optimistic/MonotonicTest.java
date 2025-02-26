/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.testsuite.repository.optimistic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.common.transaction.IsolationLevel;
import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.testsuite.repository.OptimisticIsolationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MonotonicTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		System.setProperty("org.eclipse.rdf4j.repository.debug", "true");
	}

	private Repository repo;

	private RepositoryConnection a;

	private RepositoryConnection b;

	private IsolationLevel level = IsolationLevels.SNAPSHOT_READ;

	private String NS = "http://rdf.example.org/";

	private ValueFactory lf;

	private IRI PAINTER;

	private IRI PAINTS;

	private IRI PAINTING;

	private IRI YEAR;

	private IRI PERIOD;

	private IRI PICASSO;

	private IRI REMBRANDT;

	private IRI GUERNICA;

	private IRI JACQUELINE;

	private IRI NIGHTWATCH;

	private IRI ARTEMISIA;

	private IRI DANAE;

	private IRI JACOB;

	private IRI ANATOMY;

	private IRI BELSHAZZAR;

	@Before
	public void setUp() throws Exception {
		repo = OptimisticIsolationTest.getEmptyInitializedRepository(MonotonicTest.class);
		lf = repo.getValueFactory();
		ValueFactory uf = repo.getValueFactory();
		PAINTER = uf.createIRI(NS, "Painter");
		PAINTS = uf.createIRI(NS, "paints");
		PAINTING = uf.createIRI(NS, "Painting");
		YEAR = uf.createIRI(NS, "year");
		PERIOD = uf.createIRI(NS, "period");
		PICASSO = uf.createIRI(NS, "picasso");
		REMBRANDT = uf.createIRI(NS, "rembrandt");
		GUERNICA = uf.createIRI(NS, "guernica");
		JACQUELINE = uf.createIRI(NS, "jacqueline");
		NIGHTWATCH = uf.createIRI(NS, "nightwatch");
		ARTEMISIA = uf.createIRI(NS, "artemisia");
		DANAE = uf.createIRI(NS, "danaë");
		JACOB = uf.createIRI(NS, "jacob");
		ANATOMY = uf.createIRI(NS, "anatomy");
		BELSHAZZAR = uf.createIRI(NS, "belshazzar");
		a = repo.getConnection();
		b = repo.getConnection();
	}

	@After
	public void tearDown() throws Exception {
		try {
			a.close();
		} finally {
			try {
				b.close();
			} finally {
				repo.shutDown();
			}
		}
	}

	@Test
	public void test_independentPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(a, PICASSO, RDF.TYPE, PAINTER, false));
		assertEquals(1, size(b, REMBRANDT, RDF.TYPE, PAINTER, false));
		a.commit();
		b.commit();
		assertEquals(2, size(a, null, RDF.TYPE, PAINTER, false));
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
	}

	@Test
	public void test_safePattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(a, null, RDF.TYPE, PAINTER, false));
		a.commit();
		b.commit();
	}

	@Test
	public void test_afterPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(a, null, RDF.TYPE, PAINTER, false));
		a.commit();
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
		b.commit();
	}

	@Test
	public void test_afterInsertDataPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <picasso> a <Painter> }", NS).execute();
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT DATA { <rembrandt> a <Painter> }", NS).execute();
		assertEquals(1, size(a, null, RDF.TYPE, PAINTER, false));
		a.commit();
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
		b.commit();
	}

	@Test
	public void test_changedPattern() throws Exception {
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		assertEquals(1, size(b, null, RDF.TYPE, PAINTER, false));
		a.commit();
		assertEquals(2, size(b, null, RDF.TYPE, PAINTER, false));
		b.commit();
	}

	@Test
	public void test_safeQuery() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting " + "WHERE { [a <Painter>] <paints> ?painting }");
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_safeInsert() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting a <Painting> }\n" + "WHERE { [a <Painter>] <paints> ?painting }", NS).execute();
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_mergeQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting " + "WHERE { [a <Painter>] <paints> ?painting }");
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		assertEquals(3, size(b, REMBRANDT, PAINTS, null, false));
		b.commit();
		assertEquals(3, size(a, null, RDF.TYPE, PAINTING, false));
	}

	@Test
	public void test_mergeInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting a <Painting> }\n" + "WHERE { [a <Painter>] <paints> ?painting }", NS).execute();
		a.commit();
		assertEquals(3, size(b, REMBRANDT, PAINTS, null, false));
		b.commit();
		assertEquals(3, size(a, null, RDF.TYPE, PAINTING, false));
	}

	@Test
	public void test_changedQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b, "SELECT ?painting " + "WHERE { [a <Painter>] <paints> ?painting }");
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		assertEquals(5, size(b, null, PAINTS, null, false));
		b.commit();
		assertEquals(3, size(a, null, RDF.TYPE, PAINTING, false));
	}

	@Test
	public void test_changedInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting a <Painting> }\n" + "WHERE { [a <Painter>] <paints> ?painting }", NS).execute();
		a.commit();
		assertEquals(5, size(b, null, PAINTS, null, false));
		b.commit();
		assertEquals(3, size(a, null, RDF.TYPE, PAINTING, false));
	}

	@Test
	public void test_safeOptionalQuery() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { ?painter a <Painter> " + "OPTIONAL { ?painter <paints> ?painting } }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_safeOptionalInsert() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO is *not* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n" + "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }", NS).execute();
		a.commit();
		b.commit();
		assertEquals(9, size(a, null, null, null, false));
		assertEquals(9, size(b, null, null, null, false));
	}

	@Test
	public void test_mergeOptionalQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { ?painter a <Painter> " + "OPTIONAL { ?painter <paints> ?painting } }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		assertEquals(3, size(b, REMBRANDT, PAINTS, null, false));
		b.commit();
		assertEquals(10, size(a, null, null, null, false));
	}

	@Test
	public void test_mergeOptionalInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n" + "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }", NS).execute();
		a.commit();
		assertEquals(3, size(b, REMBRANDT, PAINTS, null, false));
		b.commit();
		assertEquals(10, size(a, null, null, null, false));
	}

	@Test
	public void test_changedOptionalQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { ?painter a <Painter> " + "OPTIONAL { ?painter <paints> ?painting } }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		assertEquals(5, size(b, null, PAINTS, null, false));
		b.commit();
		assertEquals(10, size(a, null, null, null, false));
	}

	@Test
	public void test_changedOptionalInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		// PICASSO *is* a known PAINTER
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL, "INSERT { ?painting a <Painting> }\n" + "WHERE { ?painter a <Painter> "
				+ "OPTIONAL { ?painter <paints> ?painting } }", NS).execute();
		a.commit();
		assertEquals(5, size(b, null, PAINTS, null, false));
		b.commit();
		assertEquals(10, size(a, null, null, null, false));
	}

	@Test
	public void test_safeFilterQuery() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { ?painter a <Painter>; <paints> ?painting "
						+ "FILTER  regex(str(?painter), \"rem\", \"i\") }");
		for (Value painting : result) {
			b.add((Resource) painting, RDF.TYPE, PAINTING);
		}
		a.commit();
		b.commit();
		assertEquals(10, size(a, null, null, null, false));
	}

	@Test
	public void test_safeFilterInsert() throws Exception {
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting a <Painting> }\n" + "WHERE { ?painter a <Painter>; <paints> ?painting "
						+ "FILTER  regex(str(?painter), \"rem\", \"i\") }",
				NS).execute();
		a.commit();
		b.commit();
		assertEquals(10, size(a, null, null, null, false));
	}

	@Test
	public void test_mergeOptionalFilterQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(GUERNICA, RDF.TYPE, PAINTING);
		a.add(JACQUELINE, RDF.TYPE, PAINTING);
		List<Value> result = eval("painting", b, "SELECT ?painting " + "WHERE { [a <Painter>] <paints> ?painting "
				+ "OPTIONAL { ?painting a ?type  } FILTER (!bound(?type)) }");
		assertEquals(5, result.size());
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		assertEquals(5, size(b, null, PAINTS, null, false));
		b.commit();
		assertEquals(12, size(a, null, null, null, false));
	}

	@Test
	public void test_mergeOptionalFilterInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(GUERNICA, RDF.TYPE, PAINTING);
		a.add(JACQUELINE, RDF.TYPE, PAINTING);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting a <Painting> }\n" + "WHERE { [a <Painter>] <paints> ?painting "
						+ "OPTIONAL { ?painting a ?type  } FILTER (!bound(?type)) }",
				NS).execute();
		a.commit();
		assertEquals(5, size(b, null, PAINTS, null, false));
		b.commit();
		assertEquals(12, size(a, null, null, null, false));
	}

	@Test
	public void test_changedOptionalFilterQuery() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(GUERNICA, RDF.TYPE, PAINTING);
		a.add(JACQUELINE, RDF.TYPE, PAINTING);
		List<Value> result = eval("painting", b, "SELECT ?painting " + "WHERE { [a <Painter>] <paints> ?painting "
				+ "OPTIONAL { ?painting a ?type  } FILTER (!bound(?type)) }");
		for (Value painting : result) {
			if (painting != null) {
				b.add((Resource) painting, RDF.TYPE, PAINTING);
			}
		}
		a.commit();
		assertEquals(5, size(b, null, RDF.TYPE, PAINTING, false));
		b.commit();
		assertEquals(12, size(a, null, null, null, false));
	}

	@Test
	public void test_changedOptionalFilterInsert() throws Exception {
		a.add(PICASSO, RDF.TYPE, PAINTER);
		a.add(PICASSO, PAINTS, GUERNICA);
		a.add(PICASSO, PAINTS, JACQUELINE);
		b.add(REMBRANDT, RDF.TYPE, PAINTER);
		b.add(REMBRANDT, PAINTS, NIGHTWATCH);
		b.add(REMBRANDT, PAINTS, ARTEMISIA);
		b.add(REMBRANDT, PAINTS, DANAE);
		a.begin(level);
		b.begin(level);
		a.add(GUERNICA, RDF.TYPE, PAINTING);
		a.add(JACQUELINE, RDF.TYPE, PAINTING);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting a <Painting> }\n" + "WHERE { [a <Painter>] <paints> ?painting "
						+ "OPTIONAL { ?painting a ?type  } FILTER (!bound(?type)) }",
				NS).execute();
		assertEquals(5, size(b, null, RDF.TYPE, PAINTING, false));
		b.commit();
		a.commit();
		assertEquals(12, size(a, null, null, null, false));
	}

	@Test
	public void test_safeRangeQuery() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
						+ "FILTER  (1631 <= ?year && ?year <= 1635) }");
		for (Value painting : result) {
			b.add((Resource) painting, PERIOD, lf.createLiteral("First Amsterdam period"));
		}
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.commit();
		b.commit();
		assertEquals(17, size(a, null, null, null, false));
	}

	@Test
	public void test_safeRangeInsert() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting <period> \"First Amsterdam period\" }\n"
						+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
						+ "FILTER  (1631 <= ?year && ?year <= 1635) }",
				NS).execute();
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.commit();
		b.commit();
		assertEquals(17, size(a, null, null, null, false));
	}

	@Test
	public void test_mergeRangeQuery() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
						+ "FILTER  (1631 <= ?year && ?year <= 1635) }");
		for (Value painting : result) {
			b.add((Resource) painting, PERIOD, lf.createLiteral("First Amsterdam period"));
		}
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.commit();
		assertEquals(2, size(b, ARTEMISIA, null, null, false));
		b.commit();
		assertEquals(16, size(a, null, null, null, false));
	}

	@Test
	public void test_mergeRangeInsert() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting <period> \"First Amsterdam period\" }\n"
						+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
						+ "FILTER  (1631 <= ?year && ?year <= 1635) }",
				NS).execute();
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.commit();
		assertEquals(2, size(b, ARTEMISIA, null, null, false));
		b.commit();
		assertEquals(16, size(a, null, null, null, false));
	}

	@Test
	public void test_changedRangeQuery() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		List<Value> result = eval("painting", b,
				"SELECT ?painting " + "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
						+ "FILTER  (1631 <= ?year && ?year <= 1635) }");
		for (Value painting : result) {
			b.add((Resource) painting, PERIOD, lf.createLiteral("First Amsterdam period"));
		}
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.commit();
		assertEquals(6, size(b, REMBRANDT, PAINTS, null, false));
		b.commit();
		assertEquals(16, size(a, null, null, null, false));
	}

	@Test
	public void test_changedRangeInsert() throws Exception {
		a.add(REMBRANDT, RDF.TYPE, PAINTER);
		a.add(REMBRANDT, PAINTS, NIGHTWATCH);
		a.add(REMBRANDT, PAINTS, ARTEMISIA);
		a.add(REMBRANDT, PAINTS, DANAE);
		a.add(REMBRANDT, PAINTS, JACOB);
		a.add(REMBRANDT, PAINTS, ANATOMY);
		a.add(ARTEMISIA, YEAR, lf.createLiteral(1634));
		a.add(NIGHTWATCH, YEAR, lf.createLiteral(1642));
		a.add(DANAE, YEAR, lf.createLiteral(1636));
		a.add(JACOB, YEAR, lf.createLiteral(1632));
		a.add(ANATOMY, YEAR, lf.createLiteral(1632));
		a.begin(level);
		b.begin(level);
		b.prepareUpdate(QueryLanguage.SPARQL,
				"INSERT { ?painting <period> \"First Amsterdam period\" }\n"
						+ "WHERE { <rembrandt> <paints> ?painting . ?painting <year> ?year "
						+ "FILTER  (1631 <= ?year && ?year <= 1635) }",
				NS).execute();
		a.add(REMBRANDT, PAINTS, BELSHAZZAR);
		a.add(BELSHAZZAR, YEAR, lf.createLiteral(1635));
		a.commit();
		assertEquals(6, size(b, REMBRANDT, PAINTS, null, false));
		b.commit();
		assertEquals(16, size(a, null, null, null, false));
	}

	private int size(RepositoryConnection con, Resource subj, IRI pred, Value obj, boolean inf, Resource... ctx)
			throws Exception {
		return QueryResults.asList(con.getStatements(subj, pred, obj, inf, ctx)).size();
	}

	private List<Value> eval(String var, RepositoryConnection con, String qry) throws Exception {
		try (TupleQueryResult result = con.prepareTupleQuery(QueryLanguage.SPARQL, qry, NS).evaluate()) {
			List<Value> list = new ArrayList<>();
			while (result.hasNext()) {
				list.add(result.next().getValue(var));
			}
			return list;
		}
	}
}
