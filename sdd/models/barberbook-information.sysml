package BarberBookInformation {
  private import ScalarValues::*;
  enum def BookingStatus { enum upcoming; enum completed; enum cancelled; }
  part def User { attribute email : String; attribute role : String; }
  part def Master { attribute name : String; attribute active : Boolean; }
  part def Service { attribute name : String; attribute durationMinutes : Natural = 60; }
  part def WorkDay { attribute date : String; attribute startHour : Natural; attribute endHour : Natural; }
  part def SlotBlock { attribute date : String; attribute hour : Natural; attribute reason : String; }
  part def Booking {
    attribute date : String;
    attribute hour : Natural;
    attribute status : BookingStatus;
    ref part client : User;
    ref part master : Master;
    ref part service : Service;
  }
  part def Calendar {
    part days : WorkDay [0..*];
    part blocks : SlotBlock [0..*];
    part bookings : Booking [0..*];
  }
}
