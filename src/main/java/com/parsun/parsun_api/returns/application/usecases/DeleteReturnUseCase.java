package com.parsun.parsun_api.returns.application.usecases;

import com.parsun.parsun_api.returns.domain.entities.Return;
import com.parsun.parsun_api.returns.domain.entities.ReturnId;
import com.parsun.parsun_api.returns.domain.repositories.ReturnRepository;
import jakarta.transaction.Transactional;

import java.util.UUID;

public class DeleteReturnUseCase {

    private final ReturnRepository returnRepository;

    public DeleteReturnUseCase(ReturnRepository returnRepository) {
        this.returnRepository = returnRepository;
    }

    @Transactional
    public void execute(UUID id) {
        ReturnId returnId = ReturnId.of(id);
        Return return_ = returnRepository.findById(returnId)
                .orElseThrow(() -> new GetReturnByIdUseCase.ReturnNotFoundException(id));

        returnRepository.delete(returnId);
    }
}
