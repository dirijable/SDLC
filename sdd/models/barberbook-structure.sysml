package BarberBookStructure {
  private import ScalarValues::*;
  part def WebUI;
  part def AuthService;
  part def CatalogService;
  part def ScheduleService;
  part def BookingService;
  part def ReminderService;
  part def PostgreSQL;
  part def BarberBook {
    part ui : WebUI;
    part auth : AuthService;
    part catalog : CatalogService;
    part schedule : ScheduleService;
    part booking : BookingService;
    part reminder : ReminderService;
    part db : PostgreSQL;
    connect ui to auth;
    connect ui to catalog;
    connect ui to schedule;
    connect ui to booking;
    connect booking to schedule;
    connect schedule to reminder;
    connect auth to db;
    connect catalog to db;
    connect schedule to db;
    connect booking to db;
  }
  part system : BarberBook;
}
