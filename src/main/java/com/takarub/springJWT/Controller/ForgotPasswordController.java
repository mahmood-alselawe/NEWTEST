package com.takarub.springJWT.Controller;

import com.takarub.springJWT.dto.ChangePassword;
import com.takarub.springJWT.dto.MailBody;
import com.takarub.springJWT.model.ForgotPassword;
import com.takarub.springJWT.model.User;
import com.takarub.springJWT.reposoitry.ForgotPasswordRepository;
import com.takarub.springJWT.reposoitry.UserRepository;
import com.takarub.springJWT.service.EmailServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailServices emailServices;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<?> verifyMail(@PathVariable String email) {
        User user = userRepository
                .findByUsername(email)
                .orElseThrow(() -> new RuntimeException("please provide a valid email"));

        int otp = otpGenerator();
        MailBody mailBody = MailBody
                .builder()
                .to(email)
                .text("This is the OTP for forgot password request: " + otp)
                .subject("OTP for forgot password request")
                .build();

        // Check for existing ForgotPassword record
        ForgotPassword existingFp = forgotPasswordRepository.findByUser(user);
        if (existingFp != null) {
            // Optionally, update the existing record
            existingFp.setOtp(otp);

            existingFp.setExirationDate(new Date(System.currentTimeMillis() + 100 * 1000));
            forgotPasswordRepository.save(existingFp);
        } else {
            // Create a new record if none exists
            ForgotPassword fp = ForgotPassword
                    .builder()
                    .otp(otp)
                    .exirationDate(new Date(System.currentTimeMillis() + 100 * 1000))
                    .user(user)
                    .build();
            forgotPasswordRepository.save(fp);
        }

        emailServices.sendSimpleMessage(mailBody);
        return ResponseEntity.ok("Email sent for verification");
    }

    @PostMapping("/verifyOtp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp,
                                            @PathVariable String email) {
        User user = userRepository
                .findByUsername(email)
                .orElseThrow(() -> new RuntimeException("please provide a valid email"));
        ForgotPassword fp = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email " + email));

        if (fp.getExirationDate().before(Date.from(Instant.now()))) {
            forgotPasswordRepository.deleteById(fp.getFpId());
            return new ResponseEntity<>("OTP has expired", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified!");
    }

    @PostMapping("/changePassword/{email}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String email) {
        if (!Objects.equals(changePassword.password(), changePassword.repeatPassword())) {
            return new ResponseEntity<>("Please enter the password again", HttpStatus.EXPECTATION_FAILED);
        }

        String encode = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(email, encode);

        return ResponseEntity.ok("Password has been changed");
    }

    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
