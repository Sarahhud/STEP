// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

public final class FindMeetingQuery {

    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        final int meetingTimeLength = (int) request.getDuration();
        final int numberOfConflictingMeetings = events.size();
        final Collection<String> attendees = request.getAttendees();
        final Collection<String> optionalAttendees = request.getOptionalAttendees();
        Collection<TimeRange> openTimeSlots = null;
        Collection<TimeRange> openTimeSlotsForOptionalAttendees = null;
        Collection<TimeRange> openTimeSlotsForAllAttendees = new ArrayList<TimeRange>();
        Collection<TimeRange> removeTimeSlots = null;
        Collection<TimeRange> addTimeSlots = null;

        openTimeSlots = checkEdgeCases(attendees, optionalAttendees, meetingTimeLength, numberOfConflictingMeetings);
        if (openTimeSlots != null) {
            return openTimeSlots;
        }
        openTimeSlots = new ArrayList<TimeRange>();
        openTimeSlots.add(TimeRange.WHOLE_DAY);
        openTimeSlotsForOptionalAttendees = new ArrayList<TimeRange>();
        openTimeSlotsForOptionalAttendees.add(TimeRange.WHOLE_DAY);

        for (Event event:events) {
            TimeRange timeOfEvent = event.getWhen();
            if (attendees.containsAll(event.getAttendees())) { //Checks to make sure that the attendees in the event are actually needed in the meeting.  
                openTimeSlots = checkForConflictsInSchedule(openTimeSlots, timeOfEvent, meetingTimeLength);
            }
            else if (optionalAttendees.containsAll(event.getAttendees())) {
                openTimeSlotsForOptionalAttendees = checkForConflictsInSchedule(openTimeSlotsForOptionalAttendees, timeOfEvent, meetingTimeLength);
            }
        }
        if (!openTimeSlotsForOptionalAttendees.isEmpty()) {
            for (TimeRange meeting : openTimeSlots) {
                for (TimeRange optionalAttendeeMeeting : openTimeSlotsForOptionalAttendees) {
                    System.out.println(optionalAttendeeMeeting);
                    if (optionalAttendeeMeeting.contains(meeting)) {
                        openTimeSlotsForAllAttendees.add(meeting);
                    }
                }
            }
        }
        if (!openTimeSlotsForAllAttendees.isEmpty()) {
            return openTimeSlotsForAllAttendees;
        }
        else if (attendees.isEmpty() && !optionalAttendees.isEmpty()) {
            return openTimeSlotsForOptionalAttendees;
        }
        return openTimeSlots;
    }

    public Collection<TimeRange> checkForConflictsInSchedule (Collection<TimeRange> openTimeSlots, TimeRange timeOfEvent, int meetingTimeLength) { 
        Collection<TimeRange> removeTimeSlots = new ArrayList<TimeRange>();
        Collection<TimeRange> addTimeSlots = new ArrayList<TimeRange>();
        for (TimeRange openTimeSlot : openTimeSlots) { 
            if (openTimeSlot.contains(timeOfEvent)) {
                addTimeSlots = createNewOpenTimeSlotsAroundMeeting(timeOfEvent, openTimeSlot, meetingTimeLength);
                removeTimeSlots.add(openTimeSlot);
            }
            else {
                if (openTimeSlot.contains(timeOfEvent.start()) || openTimeSlot.contains(timeOfEvent.end())) {
                    addTimeSlots = createNewOpenTimeSlotsBeforeAndAfterMeeting(timeOfEvent, openTimeSlot, meetingTimeLength);
                    removeTimeSlots.add(openTimeSlot);
                }
            }
        }

        openTimeSlots.removeAll(removeTimeSlots);
        if (addTimeSlots != null) {
            openTimeSlots.addAll(addTimeSlots);
        }
        return openTimeSlots;
    }

    public Collection<TimeRange> checkEdgeCases (Collection<String> attendees, Collection<String> optionalAttendees, int meetingTimeLength, int numberOfConflictingMeetings) {
        Collection<TimeRange> openSlots = null;
        if (attendees.isEmpty() && optionalAttendees.isEmpty()) {
            openSlots = Arrays.asList(TimeRange.WHOLE_DAY);
            return openSlots;
        }
        if (meetingTimeLength > TimeRange.WHOLE_DAY.duration()) {
            openSlots = Arrays.asList();
            return openSlots;
        }
        if (numberOfConflictingMeetings < 1) {
            openSlots = Arrays.asList(TimeRange.WHOLE_DAY);
            return openSlots;
        }
        return openSlots;
    }

    public Collection<TimeRange> createNewOpenTimeSlotsAroundMeeting (TimeRange timeOfEvent, TimeRange openTimeSlot, int meetingTimeLength) {
        Collection<TimeRange> addTimeSlots = new ArrayList<TimeRange>();
        if (timeOfEvent.start() - openTimeSlot.start() >= meetingTimeLength) {
            addTimeSlots.add(TimeRange.fromStartEnd(openTimeSlot.start(), timeOfEvent.start(), false)); 
        }
        if (openTimeSlot.end() - timeOfEvent.end() >= meetingTimeLength) {
            if (openTimeSlot.end() == TimeRange.END_OF_DAY) {
                addTimeSlots.add(TimeRange.fromStartEnd(timeOfEvent.end(), openTimeSlot.end(), true));
            } 
            else {
                addTimeSlots.add(TimeRange.fromStartEnd(timeOfEvent.end(), openTimeSlot.end(), false)); 
            }
        }
        return addTimeSlots;
    }

    public Collection<TimeRange> createNewOpenTimeSlotsBeforeAndAfterMeeting (TimeRange timeOfEvent, TimeRange openTimeSlot, int meetingTimeLength) {
        Collection<TimeRange> addTimeSlots = new ArrayList<TimeRange>();
        if (openTimeSlot.contains(timeOfEvent.start())) {
            if (timeOfEvent.start() - openTimeSlot.start() >= meetingTimeLength) {
                addTimeSlots.add(TimeRange.fromStartEnd(openTimeSlot.start(), timeOfEvent.start(), false)); 
            }
        }
        if (openTimeSlot.contains(timeOfEvent.end())) {
            if (openTimeSlot.end() == TimeRange.END_OF_DAY) {
                addTimeSlots.add(TimeRange.fromStartEnd(timeOfEvent.end(), openTimeSlot.end(), true));
            } 
            else {
                addTimeSlots.add(TimeRange.fromStartEnd(timeOfEvent.end(), openTimeSlot.end(), false)); 
            }
        }
        return addTimeSlots;
    }
}
