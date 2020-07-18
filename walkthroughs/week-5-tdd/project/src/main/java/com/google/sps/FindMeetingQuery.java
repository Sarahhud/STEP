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
import java.util.stream.Collectors;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.BitSet;
import org.paukov.combinatorics3.Generator;

/**
 * Class to find a list of possible meeting times based on a request (Required attendees and length of event).
 * Also considers optional attendees and returns the timeframes where everyone can meet if applicable.
 */
public final class FindMeetingQuery {

    /*
     * Main method, loops through the day's scheduled events to try and find an optimal meeting time for the given request.
     */
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        final int sizeOfBitSet = (int) ((int) (TimeRange.END_OF_DAY + 1 - TimeRange.START_OF_DAY) / 5);
        final int meetingTimeLength = (int) request.getDuration();
        final int numberOfConflictingMeetings = events.size();
        final Collection<String> attendees = request.getAttendees();
        final Collection<String> optionalAttendees = request.getOptionalAttendees();
        Collection<TimeRange> openTimeSlots = null;
        ArrayList<TimeRange> mandatoryAttendeeMeetings = new ArrayList<TimeRange>();
        ArrayList<BitSet> options = new ArrayList<BitSet>();
        BitSet allOpenings = new BitSet(sizeOfBitSet);

        for (int i = 0; i <= sizeOfBitSet; i++) {
            allOpenings.set(i * 5 + TimeRange.START_OF_DAY);
        }

        openTimeSlots = checkEdgeCases(
            attendees, optionalAttendees, meetingTimeLength, numberOfConflictingMeetings);
        if (openTimeSlots != null) {
            return openTimeSlots;
        }

        for (Event event : events) {
            TimeRange timeOfEvent = event.getWhen();
            //Checks to make sure that the attendees in the event are actually needed in the meeting.
            if (attendees.containsAll(event.getAttendees())) {   //Looks at people in meeting request vs people in the daily schedule of.
                mandatoryAttendeeMeetings.add(event.getWhen());
            }
            if (optionalAttendees.containsAll(event.getAttendees())) {
                ArrayList<TimeRange> temp = new ArrayList<TimeRange>();
                temp.add(event.getWhen());
                BitSet tempBit = timeToBit(temp, sizeOfBitSet * 5);
                options.add(tempBit);
            }
        }

        BitSet mandatoryAttendeeOpenings = timeToBit(mandatoryAttendeeMeetings, sizeOfBitSet);
        BitSet timesWithOptionalAttendees = optionalMeetingFinder(mandatoryAttendeeOpenings, sizeOfBitSet, meetingTimeLength, options);
        return examineAllOptions(timesWithOptionalAttendees, allOpenings, mandatoryAttendeeOpenings, meetingTimeLength);

    }

    /*
     * Before examining open meeting times, first checks edge cases.
     */
    public Collection<TimeRange> checkEdgeCases ( Collection<String> attendees, Collection<String> optionalAttendees, 
        int meetingTimeLength, int numberOfConflictingMeetings) {
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

    /*
     * Examines all options, ones for mandatory attendees, and ones with optional attendees and returns a list
     * of timeranges that works best.
     */
    public Collection<TimeRange> examineAllOptions(BitSet timesWithOptionalAttendees, BitSet allOpenings, BitSet mandatoryAttendeeOpenings, int meetingTimeLength) {
        Collection<TimeRange> finalMeetingTimes = new ArrayList<TimeRange>();
        int addS, addE, start, end;

        if (timesWithOptionalAttendees.length() > 1) {
            allOpenings.and(timesWithOptionalAttendees);
        } 
        else if (timesWithOptionalAttendees.length() == 1) {
            allOpenings.clear();
        }
        else {
            allOpenings.andNot(mandatoryAttendeeOpenings);
        }   

        int[] finalOpenings = allOpenings.stream()
                .filter(s -> (s == 0 || (!allOpenings.get(s - 5)) || !allOpenings.get(s + 5))).toArray();  

        if(finalOpenings.length == 1) {
            return finalMeetingTimes;
        }
        for (int i = 0; i < finalOpenings.length; i += 2) {
            addS = 0;
            addE = 0;
            if (finalOpenings[i] != TimeRange.START_OF_DAY)
                addS = 5;
            if (!(finalOpenings[i + 1] >= TimeRange.END_OF_DAY))
                addE = 5;
            start = finalOpenings[i] - addS;
            end = finalOpenings[i + 1] + addE;
            if (end - start >= meetingTimeLength) {
                finalMeetingTimes.add(TimeRange.fromStartEnd(start, end, false));
            }
        }
        return finalMeetingTimes;
    }

    /*
     * Converts time ranges to bitsets.
     */
    public BitSet timeToBit(ArrayList<TimeRange> meetings, int size) {
        BitSet oneSet = new BitSet(size);
        for (TimeRange meeting : meetings) {
            oneSet.set(meeting.start(), meeting.end() + 1);
        }
        return oneSet;
    }
    
    /*
     * Combines multiple bit sets into one.
     */
    public BitSet severalToOne(List<BitSet> sets, BitSet openings, int size) {
        BitSet temp = (BitSet) openings.clone();
        for (BitSet bit : sets) {
            temp.or(bit);
        }
        temp.flip(0, size + 1);
        return temp;
    }

    /*
     * Checks combinations of meetings for optional attendees combined with mandatory attendees schedules.
     */
    public BitSet optionalMeetingFinder(BitSet singleOpenings, int size, int duration, List<BitSet> options) {
        // create a schedule bit list for all optional attendees
        List<List<BitSet>> subsets = Generator.subset(options).simple().stream()
                .collect(Collectors.<List<BitSet>>toList());

        boolean isValid = false;
        int bestOption = 0;
        BitSet singleSet = new BitSet(size * 5);
        BitSet tempOptions = new BitSet(size * 5);

        // loop through to see what schedule fits most attendees
        for (List<BitSet> set : subsets) {
            isValid = false;
            if (set.size() > 0) {
                singleSet = severalToOne(set, singleOpenings, size* 5);
                BitSet temp = (BitSet) singleSet.clone();
                int[] check = temp.stream().filter(s -> temp.nextClearBit(s) - s + 2 >= duration).toArray();
                if (check.length > 0) { // ensures that optional meetinf slot >= to required meeting length
                    isValid = true;
                }
            }
            if (isValid && set.size() > bestOption) {
                bestOption = set.size();
                tempOptions = singleSet;
            }
        }

        if (singleOpenings.isEmpty() && bestOption == 1) {
            tempOptions.clear();
            tempOptions.set(0);
        }
        return tempOptions;
    }

}
