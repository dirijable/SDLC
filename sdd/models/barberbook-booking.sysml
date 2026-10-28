package BarberBookBooking {
  private import ScalarValues::*;
  action def ChooseService;
  action def ChooseMaster;
  action def ChooseAvailableSlot;
  action def ValidateSlot;
  action def SaveBooking;
  action def ShowConflict;
  action def BookAppointment {
    first start;
    then action chooseService : ChooseService;
    then action chooseMaster : ChooseMaster;
    then action chooseSlot : ChooseAvailableSlot;
    then action validate : ValidateSlot;
    then action save : SaveBooking;
    then done;
  }
}
