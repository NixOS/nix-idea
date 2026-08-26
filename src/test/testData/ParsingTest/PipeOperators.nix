# Scenarios copied from
# https://github.com/NixOS/nix/blob/e086d5d899aaefdce8cc7a509cb2971be9732b2b/tests/unit/libexpr/trivial.cc#L185-L237
{
    forwardPipe = 1 |> builtins.add 2 |> builtins.mul 3;
    #ASSERT_THAT(v, IsIntEq(9));

    backwardPipe = builtins.add 1 <| builtins.mul 2 <| 3;
    #ASSERT_THAT(v, IsIntEq(7));

    forwardPipeEvaluationOrder = 1 |> null |> (x: 2);
    #ASSERT_THAT(v, IsIntEq(2));

    backwardPipeEvaluationOrder = (x: 1) <| null <| 2;
    #ASSERT_THAT(v, IsIntEq(1));

    differentPipeOperatorsDoNotAssociate = (x: 1) <| 2 |> (x: 3);
    # ASSERT_THROW(v, ParseError);

    differentPipeOperatorsParensLeft = ((x: 1) <| 2) |> (x: 3);
    #ASSERT_THAT(v, IsIntEq(3));

    differentPipeOperatorsParensRight = (x: 1) <| (2 |> (x: 3));
    #ASSERT_THAT(v, IsIntEq(1));

    forwardPipeLowestPrecedence = false -> true |> (x: !x);
    #ASSERT_THAT(v, IsFalse());

    backwardPipeLowestPrecedence = (x: !x) <| false -> true;
    #ASSERT_THAT(v, IsFalse());

    forwardPipeStrongerThanElse = if true then 1 else 2 |> 3;
    #ASSERT_THAT(v, IsIntEq(1));

    backwardPipeStrongerThanElse = if true then 1 else 2 <| 3;
    #ASSERT_THAT(v, IsIntEq(1));
}
