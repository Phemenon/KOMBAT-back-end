package com.kombat.backend.game.Interface;

import com.kombat.backend.game.Token_Tokenizer.Token;

public interface NextPeek {
    Token Peek();
    void computeNext();
}
