package com.arabesque.comparators;

import java.util.Comparator;

import com.stimasoft.obiectivecva.models.db_classes.ObjectiveLite;

public class PhaseNameComparatorDesc implements Comparator<ObjectiveLite> {

	@Override
	public int compare(ObjectiveLite obj1, ObjectiveLite obj2) {
		return obj2.getPhaseName().compareTo(obj1.getPhaseName());
	}

}
