(ns dsql.pg
  (:require [dsql.core :as ql]
            [cheshire.core]
            [clojure.string :as str])
  (:refer-clojure :exclude [format]))

(def keywords
  #{:A :ABORT :ABS :ABSENT :ABSOLUTE :ACCESS :ACCORDING :ACOS
    :ACTION :ADA :ADD :ADMIN :AFTER :AGGREGATE :ALL :ALLOCATE
    :ALSO :ALTER :ALWAYS :ANALYSE :ANALYZE :AND :ANY :APPLICATION
    :ARE :ARRAY :ARRAY_AGG :ARRAY_MAX_CARDINALITY :AS :ASC
    :ASENSITIVE :ASIN :ASSERTION :ASSIGNMENT :ASYMMETRIC :AT
    :ATAN :ATOMIC :ATTACH :ATTRIBUTE :ATTRIBUTES :AUTHORIZATION
    :AVG :BACKWARD :BASE64 :BEFORE :BEGIN :BEGIN_FRAME :BEGIN_PARTITION
    :BERNOULLI :BETWEEN :BIGINT :BINARY :BIT :BIT_LENGTH :BLOB
    :BLOCKED :BOM :BOOLEAN :BOTH :BREADTH :BY :C :CACHE :CALL
    :CALLED :CARDINALITY :CASCADE :CASCADED :CASE :CAST :CATALOG
    :CATALOG_NAME :CEIL :CEILING :CHAIN :CHAINING :CHAR :CHARACTER
    :CHARACTERISTICS :CHARACTERS :CHARACTER_LENGTH :CHARACTER_SET_CATALOG
    :CHARACTER_SET_NAME :CHARACTER_SET_SCHEMA :CHAR_LENGTH :CHECK
    :CHECKPOINT :CLASS :CLASSIFIER :CLASS_ORIGIN :CLOB :CLOSE :CLUSTER
    :COALESCE :COBOL :COLLATE :COLLATION :COLLATION_CATALOG :COLLATION_NAME
    :COLLATION_SCHEMA :COLLECT :COLUMN :COLUMNS :COLUMN_NAME :COMMAND_FUNCTION
    :COMMAND_FUNCTION_CODE :COMMENT :COMMENTS :COMMIT :COMMITTED :CONCURRENTLY
    :CONDITION :CONDITIONAL :CONDITION_NUMBER :CONFIGURATION :CONFLICT :CONNECT
    :CONNECTION :CONNECTION_NAME :CONSTRAINT :CONSTRAINTS :CONSTRAINT_CATALOG :CONSTRAINT_NAME
    :CONSTRAINT_SCHEMA :CONSTRUCTOR :CONTAINS :CONTENT :CONTINUE :CONTROL
    :CONVERSION :CONVERT :COPY :CORR :CORRESPONDING :COS :COSH :COST :COUNT :COVAR_POP :COVAR_SAMP
    :CREATE :CROSS :CSV :CUBE :CUME_DIST :CURRENT :CURRENT_CATALOG :CURRENT_DATE :CURRENT_DEFAULT_TRANSFORM_GROUP :CURRENT_PATH
    :CURRENT_ROLE :CURRENT_ROW :CURRENT_SCHEMA :CURRENT_TIME :CURRENT_TIMESTAMP :CURRENT_TRANSFORM_GROUP_FOR_TYPE
    :CURRENT_USER :CURSOR :CURSOR_NAME :CYCLE :DATA :DATABASE :DATALINK :DATE :DATETIME_INTERVAL_CODE
    :DATETIME_INTERVAL_PRECISION :DAY :DB :DEALLOCATE :DEC :DECFLOAT :DECIMAL :DECLARE :DEFAULT
    :DEFAULTS :DEFERRABLE :DEFERRED :DEFINE :DEFINED :DEFINER :DEGREE :DELETE :DELIMITER
    :DELIMITERS :DENSE_RANK :DEPENDS :DEPTH :DEREF :DERIVED :DESC :DESCRIBE :DESCRIPTOR :DETACH
    :DETERMINISTIC :DIAGNOSTICS :DICTIONARY :DISABLE :DISCARD :DISCONNECT :DISPATCH :DISTINCT :DLNEWCOPY
    :DLPREVIOUSCOPY :DLURLCOMPLETE :DLURLCOMPLETEONLY :DLURLCOMPLETEWRITE :DLURLPATH :DLURLPATHONLY :DLURLPATHWRITE :DLURLSCHEME :DLURLSERVER :DLVALUE
    :DO :DOCUMENT :DOMAIN :DOUBLE :DROP :DYNAMIC :DYNAMIC_FUNCTION :DYNAMIC_FUNCTION_CODE :EACH
    :ELEMENT :ELSE :EMPTY :ENABLE :ENCODING :ENCRYPTED :END :END-EXEC :END_FRAME :END_PARTITION :ENFORCED :ENUM :EQUALS :ERROR
    :ESCAPE :EVENT :EVERY :EXCEPT :EXCEPTION :EXCLUDE :EXCLUDING :EXCLUSIVE :EXEC :EXECUTE :EXISTS :EXP :EXPLAIN :EXPRESSION :EXTENSION
    :EXTERNAL :EXTRACT :FALSE :FAMILY :FETCH :FILE :FILTER :FINAL :FINISH :FIRST :FIRST_VALUE :FLAG :FLOAT
    :FLOOR :FOLLOWING :FOR :FORCE :FOREIGN :FORMAT :FORTRAN :FORWARD :FOUND :FRAME_ROW :FREE :FREEZE :FROM :FS
    :FULFILL :FULL :FUNCTION :FUNCTIONS :FUSION :G :GENERAL :GENERATED :GET :GLOBAL :GO
    :GOTO :GRANT :GRANTED :GREATEST :GROUP :GROUPING :GROUPS :HANDLER :HAVING :HEADER :HEX :HIERARCHY :HOLD :HOUR
    :ID :IDENTITY :IF :IGNORE :ILIKE :IMMEDIATE :IMMEDIATELY :IMMUTABLE :IMPLEMENTATION :IMPLICIT :IMPORT :IN :INCLUDE :INCLUDING :INCREMENT
    :INDENT :INDEX :INDEXES :INDICATOR :INFINITELY :INHERIT :INHERITS :INITIAL :INITIALLY :INLINE
    :INNER :INOUT :INPUT :INSENSITIVE :INSERT :INSTANCE :INSTANTIABLE :INSTEAD :INT :INTEGER :INTEGRITY :INTERSECT :INTERSECTION :INTERVAL
    :INTO :INVOKER :IS :ISNULL :ISOLATION :JOIN :JSON :JSON_ARRAY :JSON_ARRAYAGG
    :JSON_EXISTS :JSON_OBJECT :JSON_OBJECTAGG :JSON_QUERY :JSON_TABLE :JSON_TABLE_PRIMITIVE :JSON_VALUE :K :KEEP :KEY :KEYS
    :KEY_MEMBER :KEY_TYPE :LABEL :LAG :LANGUAGE :LARGE :LAST :LAST_VALUE :LATERAL :LEAD :LEADING
    :LEAKPROOF :LEAST :LEFT :LENGTH :LEVEL :LIBRARY :LIKE :LIKE_REGEX :LIMIT :LINK :LISTAGG :LISTEN
    :LN :LOAD :LOCAL :LOCALTIME :LOCALTIMESTAMP :LOCATION :LOCATOR :LOCK :LOCKED :LOG :LOG10
    :LOGGED :LOWER :M :MAP :MAPPING :MATCH :MATCHED :MATCHES :MATCH_NUMBER :MATCH_RECOGNIZE :MATERIALIZED :MAX :MAXVALUE
    :MEASURES :MEMBER :MERGE :MESSAGE_LENGTH :MESSAGE_OCTET_LENGTH :MESSAGE_TEXT :METHOD :MIN :MINUTE :MINVALUE :MOD :MODE :MODIFIES
    :MODULE :MONTH :MORE :MOVE :MULTISET :MUMPS :NAME :NAMES :NAMESPACE :NATIONAL :NATURAL :NCHAR :NCLOB :NESTED
    :NESTING :NEW :NEXT :NFC :NFD :NFKC :NFKD :NIL :NO :NONE :NORMALIZE :NORMALIZED :NOT :NOTHING :NOTIFY
    :NOTNULL :NOWAIT :NTH_VALUE :NTILE :NULL :NULLABLE :NULLIF :NULLS :NUMBER :NUMERIC :OBJECT :OCCURRENCES_REGEX :OCTETS :OCTET_LENGTH :OF :OFF :OFFSET
    :OIDS :OLD :OMIT :ON :ONE :ONLY :OPEN :OPERATOR :OPTION :OPTIONS :OR :ORDER :ORDERING
    :ORDINALITY :OTHERS :OUT :OUTER :OUTPUT :OVER :OVERFLOW :OVERLAPS :OVERLAY :OVERRIDING :OWNED :OWNER :P :PAD
    :PARALLEL :PARAMETER :PARAMETER_MODE :PARAMETER_NAME :PARAMETER_ORDINAL_POSITION :PARAMETER_SPECIFIC_CATALOG :PARAMETER_SPECIFIC_NAME :PARAMETER_SPECIFIC_SCHEMA :PARSER :PARTIAL :PARTITION :PASCAL :PASS :PASSING
    :PASSTHROUGH :PASSWORD :PAST :PATH :PATTERN :PER :PERCENT :PERCENTILE_CONT :PERCENTILE_DISC :PERCENT_RANK :PERIOD :PERMISSION
    :PERMUTE :PLACING :PLAN :PLANS :PLI :POLICY :PORTION :POSITION :POSITION_REGEX :POWER :PRECEDES :PRECEDING :PRECISION :PREPARE :PREPARED :PRESERVE :PRIMARY
    :PRIOR :PRIVATE :PRIVILEGES :PROCEDURAL :PROCEDURE :PROCEDURES :PROGRAM :PRUNE :PTF
    :PUBLIC :PUBLICATION :QUOTE :QUOTES :RANGE :RANK :READ :READS :REAL :REASSIGN :RECHECK :RECOVERY
    :RECURSIVE :REF :REFERENCES :REFERENCING :REFRESH :REGR_AVGX :REGR_AVGY :REGR_COUNT :REGR_INTERCEPT :REGR_R2
    :REGR_SLOPE :REGR_SXX :REGR_SXY :REGR_SYY :REINDEX :RELATIVE :RELEASE :RENAME :REPEATABLE :REPLACE :REPLICA
    :REQUIRING :RESET :RESPECT :RESTART :RESTORE :RESTRICT :RESULT :RETURN :RETURNED_CARDINALITY :RETURNED_LENGTH :RETURNED_OCTET_LENGTH :RETURNED_SQLSTATE :RETURNING
    :RETURNS :REVOKE :RIGHT :ROLE :ROLLBACK :ROLLUP :ROUTINE :ROUTINES :ROUTINE_CATALOG :ROUTINE_NAME
    :ROUTINE_SCHEMA :ROW :ROWS :ROW_COUNT :ROW_NUMBER :RULE :RUNNING :SAVEPOINT :SCALAR :SCALE
    :SCHEMA :SCHEMAS :SCHEMA_NAME :SCOPE :SCOPE_CATALOG :SCOPE_NAME :SCOPE_SCHEMA :SCROLL :SEARCH
    :SECOND :SECTION :SECURITY :SEEK :SELECT :SELECTIVE :SELF :SENSITIVE :SEQUENCE :SEQUENCES :SERIALIZABLE
    :SERVER :SERVER_NAME :SESSION :SESSION_USER :SET :SETOF :SETS :SHARE :SHOW :SIMILAR :SIMPLE
    :SIN :SINH :SIZE :SKIP :SMALLINT :SNAPSHOT :SOME :SOURCE :SPACE :SPECIFIC :SPECIFICTYPE :SPECIFIC_NAME
    :SQL :SQLCODE :SQLERROR :SQLEXCEPTION :SQLSTATE :SQLWARNING :SQRT :STABLE :STANDALONE
    :START :STATE :STATEMENT :STATIC :STATISTICS :STDDEV_POP :STDDEV_SAMP :STDIN :STDOUT :STORAGE :STORED
    :STRICT :STRING :STRIP :STRUCTURE :STYLE :SUBCLASS_ORIGIN :SUBMULTISET :SUBSCRIPTION :SUBSET :SUBSTRING :SUBSTRING_REGEX :SUCCEEDS :SUM
    :SUPPORT :SYMMETRIC :SYSID :SYSTEM :SYSTEM_TIME :SYSTEM_USER :T :TABLE :TABLES :TABLESAMPLE :TABLESPACE :TABLE_NAME
    :TAN :TANH :TEMP :TEMPLATE :TEMPORARY :TEXT :THEN :THROUGH :TIES :TIME :TIMEOUT :TIMESTAMP
    :TIMEZONE_HOUR :TIMEZONE_MINUTE :TO :TOKEN :TOP_LEVEL_COUNT :TRAILING :TRANSACTION :TRANSACTIONS_COMMITTED :TRANSACTIONS_ROLLED_BACK :TRANSACTION_ACTIVE :TRANSFORM
    :TRANSFORMS :TRANSLATE :TRANSLATE_REGEX :TRANSLATION :TREAT :TRIGGER :TRIGGER_CATALOG :TRIGGER_NAME :TRIGGER_SCHEMA :TRIM
    :TRIM_ARRAY :TRUE :TRUNCATE :TRUSTED :TYPE :TYPES :UESCAPE :UNBOUNDED :UNCOMMITTED :UNCONDITIONAL :UNDER
    :UNENCRYPTED :UNION :UNIQUE :UNKNOWN :UNLINK :UNLISTEN :UNLOGGED :UNMATCHED :UNNAMED :UNNEST :UNTIL :UNTYPED
    :UPDATE :UPPER :URI :USAGE :USER :USER_DEFINED_TYPE_CATALOG :USER_DEFINED_TYPE_CODE :USER_DEFINED_TYPE_NAME :USER_DEFINED_TYPE_SCHEMA :USING :UTF16 :UTF32 :UTF8 :VACUUM
    :VALID :VALIDATE :VALIDATOR :VALUE :VALUES :VALUE_OF :VARBINARY :VARCHAR :VARIADIC :VARYING :VAR_POP :VAR_SAMP :VERBOSE
    :VERSION :VERSIONING :VIEW :VIEWS :VOLATILE :WAITLSN :WHEN :WHENEVER :WHERE :WHITESPACE :WIDTH_BUCKET :WINDOW :WITH
    :WITHIN :WITHOUT :WORK :WRAPPER :WRITE :XML :XMLAGG :XMLATTRIBUTES :XMLBINARY :XMLCAST :XMLCOMMENT :XMLCONCAT :XMLDECLARATION :XMLDOCUMENT
    :XMLELEMENT :XMLEXISTS :XMLFOREST :XMLITERATE :XMLNAMESPACES :XMLPARSE :XMLPI :XMLQUERY :XMLROOT :XMLSCHEMA
    :XMLSERIALIZE :XMLTABLE :XMLTEXT :XMLVALIDATE :YEAR :YES :ZONE})

;; SELECT [ ALL | DISTINCT [ ON ( выражение [, ...] ) ] ]
;; [ * | выражение [ [ AS ] имя_результата ] [, ...] ]
;; [ FROM элемент_FROM [, ...] ]
;; [ WHERE условие ]
;; [ GROUP BY элемент_группирования [, ...] ]
;; [ HAVING условие [, ...] ]
;; [ WINDOW имя_окна AS ( определение_окна ) [, ...] ]
;; [ { UNION | INTERSECT | EXCEPT } [ ALL | DISTINCT ] выборка ]
;; [ ORDER BY выражение [ ASC | DESC | USING оператор ] [ NULLS { FIRST | LAST } ] [, ...] ]
;; [ LIMIT { число | ALL } ]
;; [ OFFSET начало [ ROW | ROWS ] ]
;; [ FETCH { FIRST | NEXT } [ число ] { ROW | ROWS } ONLY ]
;; [ FOR { UPDATE | NO KEY UPDATE | SHARE | KEY SHARE } [ OF имя_таблицы [, ...] ] [ NOWAIT | SKIP LOCKED ] [...] ]

(def keys-for-select
  [[:explain :pg/explain]
   [:select :pg/projection]
   [:from :pg/from]
   [:left-join-lateral :pg/join-lateral]
   [:left-join :pg/join]
   [:join :pg/join]
   [:where :pg/and]
   [:group-by :pg/group-by]
   [:having :pg/having]
   [:window :pg/window]
   [:order-by :pg/order-by]
   [:limit :pg/limit]
   [:offset :pg/offset]
   [:fetch :pg/fetch]
   [:for :pg/for]])

(defmethod ql/to-sql
  :pg/projection
  [acc opts data]
  (->> (dissoc data :ql/type)
       (ql/reduce-separated "," acc
                             (fn [acc [k node]]
                               (-> acc
                                   (ql/to-sql opts node)
                                   (conj "as" (ql/escape-ident opts k)))))))

(defmethod ql/to-sql
  :pg/explain
  [acc opts data]
  (cond->  acc
    (:analyze data) (conj "ANALYZE")))

(defmethod ql/to-sql
  :pg/group-by
  [acc opts data]
  (->> (dissoc data :ql/type)
       (ql/reduce-separated "," acc
                            (fn [acc [k node]]
                              (-> acc
                                  (conj (str "/*" (name k) "*/"))
                                  (ql/to-sql opts node))))))

(defmethod ql/to-sql
  :pg/sql
  [acc opts [_ data]]
  (conj acc data))

(defmethod ql/to-sql
  :pg/and
  [acc opts data]
  (->> (dissoc data :ql/type)
       (filter (fn [[k v]] (not (nil? v))))
       (ql/reduce-separated
        "AND" acc
        (fn [acc [k v]]
          (-> acc
              (conj (str "/*" (name k) "*/"))
              (ql/to-sql opts v))))))
(defmethod ql/to-sql
  :pg/or
  [acc opts data]
  (-> acc
      (conj "(")
      (into (->> (dissoc data :ql/type)
                 (filter (fn [[k v]] (not (nil? v))))
                 (ql/reduce-separated
                   "OR" []
                   (fn [acc [k v]]
                     (-> acc
                         (conj (str "/*" (name k) "*/"))
                         (ql/to-sql opts v))))))
      (conj ")")))

(defmethod ql/to-sql
  :or
  [acc opts [_ & data]]
  (let [acc (conj acc "(")]
    (-> (->> data
          (filter (fn [v] (not (nil? v))))
          (ql/reduce-separated
           "OR" acc
           (fn [acc v]
             (-> acc (ql/to-sql opts v)))))
        (conj ")"))))

(defmethod ql/to-sql
  :and
  [acc opts [_ & data]]
  (let [acc (conj acc "(")]
    (-> (->> data
             (filter (fn [v] (not (nil? v))))
             (ql/reduce-separated
              "AND" acc
              (fn [acc v]
                (-> acc (ql/to-sql opts v)))))
        (conj ")"))))

(defmethod ql/to-sql
  :||
  [acc opts [_ & data]]
  (let [acc (conj acc "(")]
    (-> (->> data
             (filter (fn [v] (not (nil? v))))
             (ql/reduce-separated
              "||" acc
              (fn [acc v]
                (-> acc (ql/to-sql opts v)))))
        (conj ")"))))

(defmethod ql/to-sql
  :pg/join
  [acc opts data]
  (->> (dissoc data :ql/type)
       (filter (fn [[k v]] (not (nil? v))))
       (ql/reduce-separated
        "LEFT JOIN" acc
        (fn [acc [k v]]
          (-> acc
              (conj (name (:table v)))
              (conj (name k) "ON")
              (ql/to-sql opts (ql/default-type (:on v) :pg/and)))))))

(defmethod ql/to-sql
  :pg/join-lateral
  [acc opts data]
  (->> (dissoc data :ql/type)
       (filter (fn [[k v]] (not (nil? v))))
       (ql/reduce-separated
        "LEFT JOIN LATERAL" acc
        (fn [acc [k v]]
          (-> acc
              (conj "(")
              (ql/to-sql opts (:sql v))
              (conj ")")
              (conj (name k) "ON")
              (ql/to-sql opts (ql/default-type (:on v) :pg/and)))))))

(defmethod ql/to-sql
  :pg/count*
  [acc _ _]
  (conj acc "count(*)"))

(defn strip-nils [m]
  (filterv (fn [[k v]] (not (nil? v))) m))

(defn pg-select
  [acc opts data]
  (->> keys-for-select
       (ql/reduce-acc
        acc
        (fn [acc [k default-type]]
          (let [sub-node (get data k)]
            (if (and sub-node
                     (not (and (map? sub-node) (empty? (strip-nils sub-node)))))
              (-> acc
                  (conj (str/upper-case (str/replace (name k) #"-" " ")))
                  (ql/to-sql opts (ql/default-type sub-node default-type)))
              acc))))))

(defmethod ql/to-sql
  :pg/select
  [acc opts data]
  (pg-select acc opts data))

(defmethod ql/to-sql
  :pg/sub-select
  [acc opts data]
  (-> acc
      (conj "(")
      (pg-select opts data)
      (conj ")")))


(defmethod ql/to-sql
  :pg/op
  [acc opts [op l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj (name op))
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :pg/fn
  [acc opts [f & args]]
  (let [acc (-> acc
                (conj (str (name f) "(")))]
    (->
     (->> args
          (ql/reduce-separated
           "," acc
           (fn [acc arg]
             (ql/to-sql acc opts arg))))
     (conj ")"))))

(defmethod ql/to-sql
  :pg/from
  [acc opts node]
  (->> (dissoc node :ql/type)
       (ql/reduce-separated
        "," acc
        (fn [acc [k sub-node]]
          (-> acc
              (ql/to-sql opts sub-node)
              (conj (name k)))))))


(defmethod ql/to-sql
  :pg/param
  [acc opts [_ v]]
  (conj acc ["?" v]))

(defmethod ql/to-sql
  :pg/jsonb
  [acc opts v]
  (if (= :pg/jsonb (first v))
    (conj acc (ql/string-litteral (cheshire.core/generate-string (second v))))
    (conj acc (ql/string-litteral (cheshire.core/generate-string v)))))

(defmethod ql/to-sql
  :pg/obj
  [acc opts obj]
  (let [acc (conj acc "jsonb_build_object(")
        acc (->> (dissoc obj :ql/type) 
                 (ql/reduce-separated
                  "," acc
                  (fn [acc [k sub-node]]
                    (-> acc
                        (conj (ql/string-litteral (name k)))
                        (conj ",")
                        (ql/to-sql opts sub-node)))))]
    (conj acc ")")))

(defmethod ql/to-sql
  :jsonb/array
  [acc opts arr]
  (let [acc (conj acc "jsonb_build_array(")
        acc (->> arr
                 (ql/reduce-separated
                  "," acc
                  (fn [acc sub-node]
                    (-> acc
                        (ql/to-sql opts sub-node)))))]
    (conj acc ")")))

(defmethod ql/to-sql
  :jsonb/obj
  [acc opts obj]
  (let [acc (conj acc "jsonb_build_object(")
        acc (->> (dissoc obj :ql/type) 
                 (ql/reduce-separated
                  "," acc
                  (fn [acc [k sub-node]]
                    (-> acc
                        (conj (ql/string-litteral (name k)))
                        (conj ",")
                        (ql/to-sql opts sub-node)))))]
    (conj acc ")")))


(defn alpha-num? [s]
  (some? (re-matches #"^[a-zA-Z][a-zA-Z0-9]*$" s)))

;; (alpha-num? "a123")
;; (alpha-num? "1a123")
;; (alpha-num? "a b123")

(defn- to-array-list [arr]
  (->> arr
       (mapv (fn [x]
               (let [x (if (keyword? x) (name x) x)]
                 (if (string? x)
                   (if (alpha-num? x)
                     x (str "\"" x "\""))
                   (if (number? x)
                     x
                     (assert false (pr-str x)))))))
       (str/join ",")))


(defn- to-array-value [arr]
  (str "{" (to-array-list arr) "}"))

(defn- to-array-litteral [arr]
  (str "'" (to-array-value arr) "'"))

(defmethod ql/to-sql
  :pg/array
  [acc opts [_ arr]]
  (conj acc (to-array-litteral arr)))

(defmethod ql/to-sql
  :pg/kfn
  [acc opts [f arg & args]]
  (let [acc (-> (conj acc (str (name f) "("))
                (ql/to-sql opts arg))]
    (->
     (loop [[k v & kvs] args
            acc acc]
       (let [acc (-> acc (conj (name k))
                     (ql/to-sql opts v))]
         (if (empty? kvs)
           acc
           (recur kvs acc))))
     (conj ")"))))

(defmethod ql/to-sql
  :pg/array-param
  [acc opts [_ tp arr]]
  (conj acc
        [(str "?::" (name tp) "[]")
         (to-array-value arr)]))

(defn acc-identity [acc & _]
  acc)

(defn identifier [acc opts id]
  (conj acc (ql/escape-ident (:keywords opts) id)))


(defmethod ql/to-sql
  :pg/index-expr
  [acc opts node]
  (-> acc
      (conj "(")
      (conj "EXPR???")
      (conj ")")))

(defmethod ql/to-sql
  :pg/index-with
  [acc opts node]
  (ql/parens
   acc (fn [acc]
         (->> (dissoc node :ql/type)
              (ql/reduce-separated
               "," acc
               (fn [acc [k node]]
                 (-> acc
                     (conj (ql/escape-ident opts k))
                     (conj "=")
                     (ql/to-sql opts node))))))))

(defmethod ql/to-sql
  :pg/projection
  [acc opts data]
  (->> (dissoc data :ql/type)
       (sort-by first)
       (ql/reduce-separated "," acc
                            (fn [acc [k node]]
                              (-> acc
                                  (ql/to-sql opts node)
                                  (conj "as" (ql/escape-ident opts k)))))))


(def index-keys
  [[:unique acc-identity]
   [:index identifier]
   [:if-not-exists acc-identity]
   [:on identifier]
   [:on-only identifier]
   [:using]
   [:expr :pg/index-expr]
   [:with :pg/index-with]
   [:tablespace identifier]
   [:where :pg/and]])

(defmethod ql/to-sql
  :pg/index
  [acc opts {idx :index tbl :on using :using expr :expr where :where :as node}]
  (when-not (and idx tbl expr)
    (throw (Exception. (str ":index :on :expr are required! Got " (pr-str node)))))
  (-> acc
      (conj "CREATE INDEX")
      (conj "IF NOT EXISTS")
      (conj (name idx))
      (conj "ON")
      (conj (name tbl))
      (cond-> using (conj (str "USING " (name using))))
      (conj "(")
      (ql/reduce-separated2 ","
                            (fn [acc exp]
                              (-> acc
                                  (conj "(")
                                  (ql/to-sql opts exp)
                                  (conj ")"))) expr)
      (conj ")")
      (cond-> where
        (->
         (conj "WHERE")
         (ql/to-sql opts where)))))

(defn resolve-type [x]
  (if-let [m (meta x)]
    (cond
      (:pg/op m) :pg/op
      (:pg/fn m) :pg/fn
      (:pg/obj m) :pg/obj
      (:jsonb/obj m) :jsonb/obj
      (:jsonb/array m) :jsonb/array
      (:pg/jsonb m) :pg/jsonb
      (:pg/kfn m) :pg/kfn
      (:pg/select m) :pg/select
      (:pg/sub-select m) :pg/sub-select
      :else nil)))

(defmethod ql/to-sql
  :jsonb/->
  [acc opts [_ col k]]
  (-> acc
      (ql/to-sql opts col)
      (conj "->")
      (conj (ql/string-litteral k))))

(defmethod ql/to-sql
  :jsonb/->>
  [acc opts [_ col k]]
  (-> acc
      (ql/to-sql opts col)
      (conj "->>")
      (conj (ql/string-litteral k))))

(defmethod ql/to-sql
  :jsonb/#>
  [acc opts [_ col path]]
  (-> acc
      (ql/to-sql opts col)
      (conj "#>")
      (conj (to-array-litteral path))))

(defmethod ql/to-sql
  :jsonb/#>>
  [acc opts [_ col path]]
  (-> acc
      (ql/to-sql opts col)
      (conj "#>>")
      (conj (to-array-litteral path))))

(defmethod ql/to-sql
  :jsonb/#>*
  [acc opts [_ col path]]
  (-> acc
      (ql/to-sql opts col)
      (conj "#> (")
      (ql/to-sql opts path)
      (conj ")")))

(defmethod ql/to-sql
  :jsonb/#>>*
  [acc opts [_ col path]]
  (-> acc
      (ql/to-sql opts col)
      (conj "#>> (")
      (ql/to-sql opts path)
      (conj ")")))

(defmethod ql/to-sql
  :||
  [acc opts [_ lhs rhs]]
  (-> acc
      (ql/to-sql opts lhs)
      (conj "||")
      (ql/to-sql opts rhs)))

(defn format [node]
  (ql/format {:resolve-type #'resolve-type :keywords keywords} (ql/default-type node :pg/select)))

(def keys-for-update
  [[:set :pg/set]
   [:from :pg/from]
   [:left-join-lateral :pg/join-lateral]
   [:where :pg/and]
   [:returning]])

(defn pg-update
  [acc opts data]
  (assert (:update data) "Key :update is required!")
  (let [acc (conj acc "UPDATE")
        acc (conj acc (if (map? (:update data))
                        (str/join " " (reverse (map name (first (:update data)))))
                        (name (:update data))))]
    (->> keys-for-update
        (ql/reduce-acc
         acc
         (fn [acc [k default-type]]
           (let [sub-node (get data k)]
             (if (and sub-node
                      (not (and (map? sub-node) (empty? (strip-nils sub-node)))))
               (-> acc
                   (conj (str/upper-case (str/replace (name k) #"-" " ")))
                   (ql/to-sql opts (ql/default-type sub-node default-type)))
               acc)))))))

(defmethod ql/to-sql
  :pg/update
  [acc opts data]
  (pg-update acc opts data))

(defmethod ql/to-sql
  :pg/set
  [acc opts data]
  (->> (dissoc data :ql/type)
       (ql/reduce-separated "," acc
                            (fn [acc [k node]]
                              (-> acc
                                  (conj (ql/escape-ident opts k) "=")
                                  (ql/to-sql opts node))))))

(def keys-for-delete
  [[:from :pg/from]
   [:where :pg/and]
   [:returning]])

(defn pg-delete
  [acc opts data]
  (let [acc (conj acc "DELETE")]
    (->> keys-for-delete
        (ql/reduce-acc
         acc
         (fn [acc [k default-type]]
           (let [sub-node (get data k)]
             (if (and sub-node
                      (not (and (map? sub-node) (empty? (strip-nils sub-node)))))
               (-> acc
                   (conj (str/upper-case (str/replace (name k) #"-" " ")))
                   (ql/to-sql opts (ql/default-type sub-node default-type)))
               acc)))))))

(defmethod ql/to-sql
  :pg/delete
  [acc opts data]
  (pg-delete acc opts data))

(defmethod ql/to-sql
  :resource->
  [acc opts [_ k]]
  (conj acc (str "resource->" (ql/string-litteral (name k)))))

(defmethod ql/to-sql
  :resource->
  [acc opts [_ k]]
  (conj acc (str "resource->" (ql/string-litteral (name k)))))

(defmethod ql/to-sql
  :resource->>
  [acc opts [_ k]]
  (conj acc (str "resource->>" (ql/string-litteral (name k)))))

(defmethod ql/to-sql
  :resource#>
  [acc opts [_ path]]
  (conj acc (str "resource#>" (to-array-litteral path))))

(defmethod ql/to-sql
  :resource#>>
  [acc opts [_ path]]
  (conj acc (str "resource#>>" (to-array-litteral path))))


(defmethod ql/to-sql
  :is
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "IS")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :is-not
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "IS NOT")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :ilike
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "ILIKE")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :similar-to
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "SIMILAR TO")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :in
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "IN")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :not-in
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "NOT IN")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :pg/with-ordinality
  [acc opts [_ x]]
  (-> acc
      (ql/to-sql opts x)
      (conj "WITH ORDINALITY")))


(defmethod ql/to-sql
  :pg/params-list
  [acc opts [_ params]]
  (let [acc (conj acc "(")]
    (->
     (->> params
          (ql/reduce-separated "," acc
                               (fn [acc p]
                                 (conj acc ["?" p]))))
     (conj  ")"))))

(defmethod ql/to-sql
  :not
  [acc opts [_ expr]]
  (-> acc
      (conj "NOT")
      (ql/to-sql opts expr)))

(defmethod ql/to-sql
  :not-with-parens
  [acc opts [_ expr]]
  (-> acc
      (conj "NOT (")
      (ql/to-sql opts expr)
      (conj ")")))

(defmethod ql/to-sql
  :pg/include-op
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "@>")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :pg/coalesce
  [acc opts [_ & args]]
  (-> (reduce
        (fn [acc x]
          (conj (ql/to-sql acc opts x) ","))
        (conj acc "COALESCE(")
        args)
      (drop-last)
      (vec)
      (conj ")")))

(defmethod ql/to-sql
  :=
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "=")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :!=
  [acc opts [_ l r]]
  (-> acc
      (ql/to-sql opts l)
      (conj "!=")
      (ql/to-sql opts r)))

(defmethod ql/to-sql
  :pg/cast
  [acc opts [_ expr tp]]
  (-> acc
      (conj "(")
      (ql/to-sql opts expr)
      (conj (str ")::" (name tp)))))

(defmethod ql/to-sql
  :jsonb/array_elements_text
  [acc opts [_ expr]]
  (-> acc
      (conj "jsonb_array_elements(")
      (ql/to-sql opts expr)
      (conj ")")))

(defmethod ql/to-sql
  :count*
  [acc opts [_ expr]]
  (conj acc "count(*)"))


(defmethod ql/to-sql
  :cond
  [acc opts [_ & pairs]]
  (into
    acc
    (concat
      ["( case"]
      (->> (partition-all 2 pairs)
           (mapcat (fn [pair]
                     (if (= 1 (count pair))
                       (-> ["else ("]
                           (ql/to-sql opts (first pair))
                           (conj ")"))
                       (-> ["when ("]
                           (ql/to-sql opts (first pair))
                           (conj ") then (")
                           (ql/to-sql opts (second pair))
                           (conj ")"))))))
      ["end )"])))


(defmethod ql/to-sql
  :case
  [acc opts [_ expression & pairs]]
  (into
    acc
    (concat
      (-> ["( case ("]
          (ql/to-sql opts expression)
          (conj ")"))
      (->> (partition-all 2 pairs)
           (mapcat (fn [pair]
                     (if (= 1 (count pair))
                       (-> ["else ("]
                           (ql/to-sql opts (first pair))
                           (conj ")"))
                       (-> ["when ("]
                           (ql/to-sql opts (first pair))
                           (conj ") then (")
                           (ql/to-sql opts (second pair))
                           (conj ")"))))))
      ["end )"])))

(defmethod ql/to-sql
  :pg/create-table
  [acc opts {not-ex :if-not-exists tbl :table-name cols :columns}]
  (-> acc
      (conj "CREATE TABLE")
      (cond-> not-ex (conj "IF NOT EXISTS"))
      (conj (name tbl))
      (conj "(")
      (ql/reduce-separated2 ","
                           (fn [acc [col-name col-def]]
                             (-> acc
                                 (conj (name col-name))
                                 (conj (:type col-def))
                                 (cond->
                                     (:primary-key col-def) (conj "PRIMARY KEY")))
                             ) cols)
      (conj ")")))

(defmethod ql/to-sql
  :pg/insert-select
  [acc opts {tbl :into {proj :select :as sel} :select}]
  (-> acc
      (conj "INSERT INTO")
      (conj (name tbl))
      (conj "(")
      (conj (->> (keys proj) (sort) (mapv name)  (str/join ", ")))
      (conj ")")
      (ql/to-sql opts (assoc sel :ql/type :pg/sub-select))))

(defmethod ql/to-sql
  :pg/cte
  [acc opts {with :with sel :select}]
  (-> acc
      (conj "WITH")
      (ql/reduce-separated2 "," (fn [acc [k v]]
                                  (-> acc
                                      (into [(name k) "AS" "("])
                                      (ql/to-sql opts (update v :ql/type (fn [x] (or x :pg/select))))
                                      (conj ")")))
                            (if (sequential? with) (partition 2 with) with))
      (ql/to-sql opts
                 (update sel :ql/type (fn [x] (or x :pg/select))))))

(defmethod ql/to-sql
  :pg/jsonb_set
  [acc opts [_ doc pth val & [create-missed]]]
  (-> acc
      (conj "jsonb_set(")
      (ql/to-sql opts doc)
      (conj ",")
      (conj (to-array-litteral pth))
      (conj ",")
      (ql/to-sql opts val)
      (cond-> create-missed (conj ", true"))
      (conj ")")))

(defmethod ql/to-sql
  :pg/jsonb_string
  [acc opts [_ str]]
  (-> acc
      (conj "(jsonb_build_object('s',")
      (ql/to-sql opts str)
      (conj ")->'s')")))

(defmethod ql/to-sql
  :pg/to_jsonb
  [acc opts [_ expr]]
  (-> acc
      (conj "to_jsonb(")
      (ql/to-sql opts expr)
      (conj ")")))

(defmethod ql/to-sql
  :pg/row_to_json
  [acc opts [_ v]]
  (-> acc
      (conj "row_to_json(")
      (ql/to-sql opts v)
      (conj ")")))

(defmethod ql/to-sql
  :min
  [acc opts [_ v]]
  (-> acc
      (conj "min(")
      (ql/to-sql opts v)
      (conj ")")))

(defmethod ql/to-sql
  :pg/row_to_jsonb
  [acc opts [_ v]]
  (-> acc
      (conj "row_to_json(")
      (ql/to-sql opts v)
      (conj ")::jsonb")))

(defmethod ql/to-sql
  :pg/jsonb_strip_nulls
  [acc opts [_ v]]
  (-> acc
      (conj "jsonb_strip_nulls(")
      (ql/to-sql opts v)
      (conj ")")))

(defmethod ql/to-sql
  :json_agg
  [acc opts [_ v]]
  (-> acc
      (conj "json_agg(")
      (ql/to-sql opts v)
      (conj ")")))

(defmethod ql/to-sql
  :jsonb_agg
  [acc opts [_ v]]
  (-> acc
      (conj "jsonb_agg(")
      (ql/to-sql opts v)
      (conj ")")))


(defmethod ql/to-sql
  :<>
  [acc opts [_ a b]]
  (-> acc
      (ql/to-sql opts a)
      (conj "<>")
      (ql/to-sql opts b)))

(defmethod ql/to-sql
  :pg/insert
  [acc opts {tbl :into vls :value ret :returning}]
  (let [cols (->> (keys vls) (sort))]
    (-> acc
        (conj "INSERT INTO")
        (conj (name tbl))
        (conj "(")
        (conj (->> cols (mapv name)  (str/join ", ")))
        (conj ")")
        (conj "VALUES")
        (conj "(")
        (ql/reduce-separated2 "," (fn [acc c] (ql/to-sql acc opts (get vls c))) cols)
        (conj ")")
        (cond->
            ret (-> (conj "RETURNING")
                    (ql/to-sql opts ret))))))

(defmethod ql/to-sql
  :resource||
  [acc opts [_ & exprs]]
  (-> acc 
   (conj "resource ||")
   (ql/reduce-separated2 "||" (fn [acc expr] (ql/to-sql acc opts expr)) exprs)))
