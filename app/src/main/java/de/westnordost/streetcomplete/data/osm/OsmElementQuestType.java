package de.westnordost.streetcomplete.data.osm;

import android.os.Bundle;

import de.westnordost.streetcomplete.data.QuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.osmapi.map.data.Element;

public interface OsmElementQuestType extends QuestType
{
	/** @return whether the given element matches with this quest type */
	boolean appliesTo(Element element);

	/** applies the data from answer to the given element
	 *  @return the string resource id to use as commit message */
	Integer applyAnswerTo(Bundle answer, StringMapChangesBuilder changes);
}
