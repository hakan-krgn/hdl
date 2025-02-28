package com.heretere.hdl.common.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Jacksonized
public class Repository {

    @Singular
    private List<String> urls;
}
