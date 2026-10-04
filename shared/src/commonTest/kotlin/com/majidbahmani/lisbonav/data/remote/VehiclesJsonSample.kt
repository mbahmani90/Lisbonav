package com.majidbahmani.lisbonav.data.remote

/**
 * Two vehicles captured from `GET https://api.carrismetropolitana.pt/v2/vehicles` (2026-10-05).
 * The second one has `bearing: null` (9 of 113 vehicles in the capture had no bearing).
 * Kept as a string because commonTest has no cross-platform resource loading.
 */
internal const val VEHICLES_JSON_SAMPLE = """
[
  {
    "agency_id": "41", "bearing": 104, "bikes_allowed": false, "block_id": null,
    "capacity_seated": 0, "capacity_standing": 0, "capacity_total": 0, "contactless": true,
    "current_status": "STOPPED_AT", "direction_id": 1, "door_status": "CLOSED", "emission_class": null,
    "event_id": "0972a309791938f05bb214280e39f681aa72e8218ab8ae9368cd395df3159a2c",
    "id": "[LA77N]1314", "lat": 38.686607, "license_plate": null, "line_id": "1997", "lon": -9.332203,
    "make": null, "model": null, "occupancy_estimated": null, "occupancy_status": "NO_DATA_AVAILABLE",
    "owner": null, "pattern_id": "[VNWG3][LA77N]1997_0_2", "propulsion": null, "registration_date": null,
    "route_id": "[LA77N]1997_0", "schedule_relationship": "SCHEDULED", "shift_id": null, "speed": 50,
    "stop_id": "050050", "timestamp": 1791156049000, "trip_id": "[VNWG3][LA77N]1997_0_2_2400_2429_0_73",
    "wheelchair_accessible": null
  },
  {
    "agency_id": "42", "bearing": null, "bikes_allowed": false, "block_id": null,
    "capacity_seated": 0, "capacity_standing": 0, "capacity_total": 0, "contactless": true,
    "current_status": "STOPPED_AT", "direction_id": 0, "door_status": "CLOSED", "emission_class": null,
    "event_id": "836c6ef3d948a27e9994d0a5e85417aa62c10b7207102a713877e7d33ccb48de",
    "id": "[BNA17]1211", "lat": 38.828083, "license_plate": null, "line_id": "2754", "lon": -9.164331,
    "make": null, "model": null, "occupancy_estimated": null, "occupancy_status": "NO_DATA_AVAILABLE",
    "owner": null, "pattern_id": "[TPNBR][BNA17]2754_0_1", "propulsion": null, "registration_date": null,
    "route_id": "[BNA17]2754_0", "schedule_relationship": "SCHEDULED", "shift_id": null, "speed": 0,
    "stop_id": "070035", "timestamp": 1791156048000, "trip_id": "[TPNBR][BNA17]2754_0_1|170|1|2400",
    "wheelchair_accessible": null
  }
]
"""
