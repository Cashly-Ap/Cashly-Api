package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.returns.application.dto.response.ReturnResponse;
import com.parsun.parsun_api.returns.application.mappers.ReturnMapper;
import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnId;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;

import java.util.UUID;

public class GetReturnByIdUseCase {

    private final ReturnRepository returnRepository;

    public GetReturnByIdUseCase(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    public ReturnResponse execute(UUID id) {
        Return return_ = returnRepository.findById(ReturnId.of(id))
                .orElseThrow(() -> new ReturnNotFoundException(id));

        return ReturnMapper.toResponse(return_);
    }

    public static class ReturnNotFoundException extends RuntimeException {
        public ReturnNotFoundException(UUID id) {
            super("Return not found with id: " + id);
        }
    }
}
