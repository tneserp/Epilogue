package com.epilogue.service;

import com.epilogue.domain.user.User;
import com.epilogue.domain.will.Will;
import com.epilogue.domain.witness.Witness;
import com.epilogue.dto.request.will.WillAdditionalRequestDto;
import com.epilogue.dto.request.will.WillApplyRequestDto;
import com.epilogue.dto.request.will.WillMemorialRequestDto;
import com.epilogue.repository.user.UserRepository;
import com.epilogue.repository.will.WillRepository;
import com.epilogue.repository.witness.WitnessRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WillService {
    private final WillRepository willRepository;
    private final UserRepository userRepository;
    private final WitnessRepository witnessRepository;

    public void saveWill(Will will) {
        willRepository.save(will);
    }

    @Transactional
    public void saveMemorial(WillMemorialRequestDto willMemorialRequestDto, Principal principal) {
        User user = userRepository.findByUserId(principal.getName());
        Will will = user.getWill();

        will.updateMemorial(willMemorialRequestDto.isUseMemorial(), willMemorialRequestDto.getGraveName());
    }

    @Transactional
    public void saveAdditionalInformation(WillAdditionalRequestDto willAdditionalRequestDto, Principal principal) {
        User user = userRepository.findByUserId(principal.getName());
        Will will = willRepository.findById(user.getWill().getWillSeq()).get();

        will.updateAdditionalInformation(willAdditionalRequestDto.isSustainCare(), willAdditionalRequestDto.getFuneralType(), willAdditionalRequestDto.getGraveType(), willAdditionalRequestDto.isOrganDonation());
    }

    public void viewMyWill(Principal principal) {
        String loginUserId = principal.getName();

        // S3에서 녹음 파일 가져오기

    }

    public void deleteMyWill(Principal principal) {
        String loginUserId = principal.getName();

        // S3에서 유언 파일 삭제

        // 블록체인에서 유언 파일 url 삭제

        // DB에서 유언 삭제
        User user = userRepository.findByUserId(loginUserId);
        Will will = user.getWill();
        willRepository.deleteByWillSeq(will.getWillSeq());
    }

    public boolean applyWill(WillApplyRequestDto willApplyRequestDto) {
        // 고인 이름, 고인 생년월일, 증인 이름, 증인 코드 일치 검사
        String deadName = willApplyRequestDto.getDeadName();
        String deadBirth = willApplyRequestDto.getDeadBirth();
        String witnessName = willApplyRequestDto.getWitnessName();
        String witnessCode = willApplyRequestDto.getWitnessCode();

        User user = userRepository.findByNameAndBirth(deadName, deadBirth);
        if (user == null) return false;

        Witness witness = witnessRepository.findByWitnessNameAndWitnessCode(witnessName, witnessCode);
        if (witness == null) return false;

        int deadWillSeq = user.getWill().getWillSeq();
        int witnessWillSeq = witness.getWill().getWillSeq();

        if (deadWillSeq == witnessWillSeq) return true;
        else return false;
    }
}
