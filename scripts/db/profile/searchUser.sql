SELECT
    ext_id AS id,
    user_name AS name,
    nickname AS nickName,
    is_male AS isMale,
    is_auto_user AS autoUser,
    created_at AS createdAt,
    updated_at AS updatedAt

FROM OA_USER
WHERE
    is_active = 1
    AND
    (
    email LIKE :email
    <if(hasName)>
    OR
    user_name LIKE :name
    <endif>
    )