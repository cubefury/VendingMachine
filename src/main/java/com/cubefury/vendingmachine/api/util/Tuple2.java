package com.cubefury.vendingmachine.api.util;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record Tuple2<A, B> (A first, B second) {}
