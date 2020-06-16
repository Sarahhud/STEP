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
import java.io.*;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    if (request.getAttendees().isEmpty()) {
        Collection<TimeRange> range = Arrays.asList(TimeRange.WHOLE_DAY);
        return range;
    }
    long meetingTime = request.getDuration();
    Collection<String> attendees = request.getAttendees();
    if (meetingTime > TimeRange.WHOLE_DAY.duration()) {
        Collection<TimeRange> range = Arrays.asList();
        return range;
    }
    if (events.size() < 1){
        Collection<TimeRange> range = Arrays.asList(TimeRange.WHOLE_DAY);
        return range;
    }

    Collection<TimeRange> openTimeSlots = new ArrayList<TimeRange>();
    Collection<TimeRange> removeTimeSlots = new ArrayList<TimeRange>();
    openTimeSlots.add(TimeRange.WHOLE_DAY);

    for(Event event:events){
        if(!Collections.disjoint(event.getAttendees(), attendees)){
            TimeRange timeOfEvent = event.getWhen();
            for(TimeRange openTimeSlot : openTimeSlots){ 
                System.out.println(openTimeSlot.start() + " " + openTimeSlot.end());
                if(openTimeSlot.contains(timeOfEvent)){
                    System.out.println("Event in open time block!");
                    if (timeOfEvent.start() - openTimeSlot.start() >= meetingTime) {
                        TimeRange a = TimeRange.fromStartEnd(openTimeSlot.start(), timeOfEvent.start(), false); 
                        openTimeSlots.add(a);
                    }
                    if (openTimeSlot.end() - timeOfEvent.end() >= meetingTime) {
                        if(openTimeSlot.end() == TimeRange.END_OF_DAY){
                            TimeRange b = TimeRange.fromStartEnd(timeOfEvent.end(), openTimeSlot.end(), true);
                            openTimeSlots.add(b);
                        } 
                        else {
                            TimeRange c = TimeRange.fromStartEnd(timeOfEvent.end(), openTimeSlot.end(), false); 
                            openTimeSlots.add(c);
                        }
                    }
                    removeTimeSlots.add(openTimeSlot);
                }
                //What if it overlaps??
            }
            openTimeSlots.removeAll(removeTimeSlots);
        }
    }
    return openTimeSlots;
  }
}
