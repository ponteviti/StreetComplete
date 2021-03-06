package de.westnordost.streetcomplete.quests.opening_hours;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.util.SerializedSavedState;

public class OpeningHoursPerMonth extends LinearLayout implements OpeningHoursFormRoot
{
	private static final int MAX_MONTH_INDEX = 11;

	private Map<OpeningHoursPerWeek, CircularSection> ranges = new HashMap<>();
	private Button btnAdd;
	private ViewGroup rows;

	public OpeningHoursPerMonth(Context context)
	{
		super(context);
		init();
	}

	public OpeningHoursPerMonth(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.list_with_bottom_add_btn, this, true);

		btnAdd = (Button) findViewById(R.id.btn_add);
		btnAdd.setText(R.string.quest_openingHours_add_months);
		btnAdd.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				add();
			}
		});
		rows = (ViewGroup) findViewById(R.id.rows);
	}

	/** Open dialog to let the user specify the range and add this new row*/
	public void add()
	{
		CircularSection range = getRangeSuggestion();
		openSetRangeDialog(
				new RangePickerDialog.OnRangeChangeListener()
				{
					@Override public void onRangeChange(int startIndex, int endIndex)
					{
						add(new CircularSection(startIndex, endIndex)).add();
					}
				}, range
		);
	}

	/** add a new row with the given range */
	public OpeningHoursPerWeek add(CircularSection months)
	{
		final LayoutInflater inflater = LayoutInflater.from(getContext());
		final ViewGroup row = (ViewGroup) inflater.inflate(R.layout.quest_opening_hours_month_row, rows, false);

		View btnDelete = row.findViewById(R.id.delete);
		btnDelete.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				remove(row);
			}
		});

		final TextView fromTo = (TextView) row.findViewById(R.id.months_from_to);
		final OpeningHoursPerWeek openingHoursPerWeek =
				(OpeningHoursPerWeek) row.findViewById(R.id.weekday_select_container);

		fromTo.setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				CircularSection range = ranges.get(openingHoursPerWeek);

				openSetRangeDialog(	new RangePickerDialog.OnRangeChangeListener()
				{
					@Override public void onRangeChange(int startIndex, int endIndex)
					{
						putData(fromTo, openingHoursPerWeek, new CircularSection(startIndex, endIndex));
					}
				}, range);
			}
		});

		putData(fromTo, openingHoursPerWeek, months);
		rows.addView(row);
		return openingHoursPerWeek;
	}

	public void remove(ViewGroup view)
	{
		OpeningHoursPerWeek child =	(OpeningHoursPerWeek) view.findViewById(R.id.weekday_select_container);
		ranges.remove(child);
		rows.removeView(view);
	}

	@Override public Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();
		return new SerializedSavedState(superState, getAll());
	}

	@Override public void onRestoreInstanceState(Parcelable state)
	{
		SerializedSavedState savedState = (SerializedSavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		addAll((HashMap) savedState.get(HashMap.class));
	}

	@Override protected void dispatchSaveInstanceState(SparseArray<Parcelable> container)
	{
		super.dispatchFreezeSelfOnly(container);
	}

	@Override protected void dispatchRestoreInstanceState(SparseArray container)
	{
		super.dispatchThawSelfOnly(container);
	}

	public HashMap<CircularSection, HashMap<Weekdays, ArrayList<TimeRange>>> getAll()
	{
		HashMap<CircularSection, HashMap<Weekdays, ArrayList<TimeRange>>> result =
				new HashMap<>(2); // madness!!
		for (Map.Entry<OpeningHoursPerWeek, CircularSection> e : ranges.entrySet())
		{
			result.put(e.getValue(), e.getKey().getAll());
		}
		return result;
	}

	public void addAll(
			HashMap<CircularSection, HashMap<Weekdays, ArrayList<TimeRange>>> data)
	{
		ArrayList<CircularSection> sortedKeys = new ArrayList<>(data.keySet());
		Collections.sort(sortedKeys);
		for(CircularSection range : sortedKeys)
		{
			OpeningHoursPerWeek weekView = add(range);
			weekView.addAll(data.get(range));
		}
	}

	private @NonNull CircularSection getRangeSuggestion()
	{
		List<CircularSection> months = getUnmentionedMonths();
		if(months.isEmpty())
		{
			return new CircularSection(0,MAX_MONTH_INDEX);
		}
		return months.get(0);
	}

	private List<CircularSection> getUnmentionedMonths()
	{
		return new NumberSystem(0,MAX_MONTH_INDEX).complemented(ranges.values());
	}

	@Override public String getOpeningHoursString()
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (Map.Entry<OpeningHoursPerWeek, CircularSection> entry : ranges.entrySet())
		{
			CircularSection range = entry.getValue();

			OpeningHoursPerWeek weeklyOpeningHours = entry.getKey();

			// the US locale is important here as this is the OSM format for dates
			String monthsString = getMonthsString(range, DateFormatSymbols.getInstance(Locale.US).getShortMonths(), "-", ": ");
			String monthRangeString = weeklyOpeningHours.getOpeningHoursString(monthsString);

			if(!monthRangeString.isEmpty())
			{
				if(!first)	result.append("; ");
				else		first = false;

				result.append(monthRangeString);
			}
		}

		return result.toString();
	}

	private void openSetRangeDialog(RangePickerDialog.OnRangeChangeListener callback,
									CircularSection months)
	{
		String[] monthNames = DateFormatSymbols.getInstance().getMonths();
		String selectMonths = getResources().getString(R.string.quest_openingHours_chooseMonthsTitle);
		new RangePickerDialog(getContext(), callback, monthNames, months.getStart(),
				months.getEnd(), selectMonths).show();
	}

	private void putData(TextView view, OpeningHoursPerWeek child, CircularSection months)
	{
		view.setText(getMonthsString(months, DateFormatSymbols.getInstance().getMonths(), "–", ": "));
		ranges.put(child, months);
	}

	private String getMonthsString(CircularSection months, String[] names, String range, String colon)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(names[months.getStart()]);
		if(months.getStart() != months.getEnd())
		{
			sb.append(range);
			sb.append(names[months.getEnd()]);
		}
		sb.append(colon);
		return sb.toString();
	}
}
