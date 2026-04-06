# cooked-dinner (CMPUT 301 Team Project)

## Project Overview

Cooked Dinner is an Android event lottery system application designed to help community
organizations manage registrations for high demand events in a fair and accessible way.

Instead of requiring users to register on a first come basis, the system allows entrants
to join a waiting list during a registration window. After the window closes, organizers
can randomly select participants using a lottery system. Selected entrants are notified
and may accept or decline their invitation, after which replacement entrants may be drawn
automatically.

The application supports QR based event discovery, waitlist pooling, notifications,
poster uploads, optional geolocation verification, and role based access for entrants,
organizers, and administrators.


## Features

### Entrant Features

- Join and leave event waiting lists
- Browse and search events
- Filter events by availability and capacity
- Scan QR codes to view event details
- Receive lottery result notifications
- Accept or decline event invitations
- View event participation history
- Manage personal profile information
- Comment on events
- Opt in/out of notifications

### Organizer Features

- Create public and private events
- Generate promotional QR codes
- Upload and update event posters
- Set registration windows
- View waiting list entrants
- Enable or disable geolocation requirements
- Sample entrants from waiting lists
- Draw replacement entrants when needed
- Send notifications to entrants
- Export enrolled entrant lists (CSV)
- Assign co-organizers
- Moderate event comments

### Administrator Features

- Remove events, profiles, and images
- Browse events and user profiles
- Review uploaded images
- Remove inappropriate organizers
- Review logs of notifications
- Remove event comments
- Act as entrant and/or organizer within the system


## Technologies Used

The application is implemented using:

- Java
- Android Studio
- Firebase Firestore (database)
- Firebase Storage (poster uploads)
- Google Maps API (geolocation features)
- QR scanning libraries (Google QR tools)


## Repository Structure

app/  
 Android application source code

docs/ui/mockups/  
 UI mockups and interface sketches

docs/ui/storyboards/  
 Storyboard sequences

docs/ui/decisions/  
 UI decision documentation

docs/crc/  
 CRC card images

docs/  
 Supporting documentation assets


## Setup Instructions

To run the project locally:

1. Clone the repository
2. Open the project in Android Studio
3. Add your Firebase configuration file:

   app/google-services.json

4. Add your Google Maps API key to:

   local.properties

5. Sync Gradle and run the application on an emulator or Android device


## System Roles

The system supports three primary actor roles:

Entrant  
A user who joins event waiting lists and participates in lotteries

Organizer  
A user who creates events, manages waiting lists, and selects participants

Administrator  
A privileged user responsible for moderation and infrastructure oversight


## Current Status

Project Part 4 — Final Checkpoint

Includes:

- updated UI mockups
- storyboard sequences
- updated CRC cards
- structured GitHub workflow
- issue tracking via project board
