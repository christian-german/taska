import schemathesis


@schemathesis.hook.apply_to(
    operation_id=["createPlanningCalendar", "updatePlanningCalendar"]
)
def filter_case(ctx, case):
    body = case.body
    rules = body.get("rules")
    if not rules:
        return True

    return all(rule["startMinute"] < rule["endMinute"] for rule in rules)
