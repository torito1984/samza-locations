/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.dmr.doyle.samza.nlp;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author David Martinez
 */
public class NLPConceptExtractor {

    private AbstractSequenceClassifier<CoreLabel> classifier;
    public static final String DEFAULT_CLASSIFIER = "classifiers/english.all.3class.distsim.crf.ser.gz";
    public static final String PERSON = "PERSON";
    public static final String ORGANIZATION = "ORGANIZATION";
    public static final String LOCATION = "LOCATION";

    public NLPConceptExtractor(String pathToClassifier) throws IOException, ClassNotFoundException {
        classifier = CRFClassifier.getClassifier(pathToClassifier);
    }

    public NLPConceptExtractor() throws IOException, ClassNotFoundException {
        classifier = CRFClassifier.getClassifier(DEFAULT_CLASSIFIER);
    }

    private List<String> getElements(String text, Predicate<Triple<String, Integer, Integer>> f) {
        return classifier.classifyToCharacterOffsets(text).stream()
                .filter(f)
                .map(item ->  text.substring(item.second(), item.third()))
                .collect(Collectors.toList());
    }

    public List<String> getPersons(String text) {
        return getElements(text, i -> i.first().compareTo(PERSON) == 0);
    }

    public List<String> getLocations(String text) {
        return getElements(text, i -> i.first().compareTo(LOCATION) == 0);
    }

    public List<String> getOrganizations(String text) {
        return getElements(text, i -> i.first().compareTo(ORGANIZATION) == 0);
    }
}
