package com.java.hospitalmanagementsystem.services.Implementation;

import com.java.hospitalmanagementsystem.models.EntertainmentType;
import com.java.hospitalmanagementsystem.models.Payment;
import com.java.hospitalmanagementsystem.models.RoomType;
import com.java.hospitalmanagementsystem.models.User;
import com.java.hospitalmanagementsystem.models.dto.payment.CreatePayment;
import com.java.hospitalmanagementsystem.repositories.EntertainmentTypeRepository;
import com.java.hospitalmanagementsystem.repositories.PaymentRepository;
import com.java.hospitalmanagementsystem.repositories.RoomTypeRepository;
import com.java.hospitalmanagementsystem.security.SecurityTools;
import com.java.hospitalmanagementsystem.services.BookingService;
import com.java.hospitalmanagementsystem.services.PaymentService;
import com.java.hospitalmanagementsystem.util.Tools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Implementation for managing payment processing in the application.
 *
 * <p>This service provides methods for calculating payment amounts, creating reservations, and
 * processing actual payments. It handles various aspects of payment from computation to final
 * transaction creation.
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {

  private final RoomTypeRepository roomTypeRepository;
  private final EntertainmentTypeRepository entertainmentTypeRepository;
  private final PaymentRepository paymentRepository;
  private final BookingService bookingService;
  private final SecurityTools securityTools;

  @Override
  public int getPaymentAmount(CreatePayment createPayment) {
    User user = securityTools.retrieveUserData();
    int discount = user.getDiscount();
    if (createPayment.getPaymentType().equals("Room")) {
      Optional<RoomType> roomType = roomTypeRepository.findById(createPayment.getRoomTypeId());
      if (roomType.isEmpty()) {
        throw new IllegalArgumentException("Room type not found");
      }
      int amount =
          Tools.getRoomPaymentAmount(
              roomType.get(), createPayment.getDateFrom(), createPayment.getDateTo());
      return amount - (amount * discount / 100);
    } else {
      Optional<EntertainmentType> entertainmentType =
          entertainmentTypeRepository.findByName(createPayment.getPaymentType());
      if (entertainmentType.isEmpty()) {
        throw new IllegalArgumentException("Entertainment type not found");
      }
      int amount =
          Tools.getEntertainmentPaymentAmount(
              entertainmentType.get(),
              createPayment.getDateFrom(),
              createPayment.getTimeFrom(),
              createPayment.getDateTo(),
              createPayment.getTimeTo());
      return amount - (amount * discount / 100);
    }
  }

  @Override
  public int createReservation(CreatePayment createPayment) {
    if (createPayment.getPaymentType().equals("Room")) {
      return bookingService
          .bookRoom(
              createPayment.getDateFrom(), createPayment.getDateTo(), createPayment.getRoomTypeId())
          .getId();
    } else {
      return bookingService
          .bookEntertainment(
              createPayment.getPaymentType(),
              createPayment.getDateFrom(),
              createPayment.getTimeFrom(),
              createPayment.getDateTo(),
              createPayment.getTimeTo(),
              createPayment.getEntertainmentId())
          .getId();
    }
  }

  @Override
  public Payment createPayment(int amount) {
    Payment payment = new Payment(new Timestamp(System.currentTimeMillis()), amount);
    paymentRepository.save(payment);
    return payment;
  }
}
