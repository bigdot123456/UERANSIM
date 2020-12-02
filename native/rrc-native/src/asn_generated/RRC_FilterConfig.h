/*
 * Generated by asn1c-0.9.29 (http://lionet.info/asn1c)
 * From ASN.1 module "NR-RRC-Definitions"
 * 	found in "asn/nr-rrc-15.6.0.asn1"
 * 	`asn1c -fcompound-names -pdu=all -findirect-choice -fno-include-deps -gen-PER -no-gen-OER -no-gen-example -D rrc`
 */

#ifndef	_RRC_FilterConfig_H_
#define	_RRC_FilterConfig_H_


#include <asn_application.h>

/* Including external dependencies */
#include "RRC_FilterCoefficient.h"
#include <constr_SEQUENCE.h>

#ifdef __cplusplus
extern "C" {
#endif

/* RRC_FilterConfig */
typedef struct RRC_FilterConfig {
	RRC_FilterCoefficient_t	*filterCoefficientRSRP;	/* DEFAULT 4 */
	RRC_FilterCoefficient_t	*filterCoefficientRSRQ;	/* DEFAULT 4 */
	RRC_FilterCoefficient_t	*filterCoefficientRS_SINR;	/* DEFAULT 4 */
	
	/* Context for parsing across buffer boundaries */
	asn_struct_ctx_t _asn_ctx;
} RRC_FilterConfig_t;

/* Implementation */
extern asn_TYPE_descriptor_t asn_DEF_RRC_FilterConfig;
extern asn_SEQUENCE_specifics_t asn_SPC_RRC_FilterConfig_specs_1;
extern asn_TYPE_member_t asn_MBR_RRC_FilterConfig_1[3];

#ifdef __cplusplus
}
#endif

#endif	/* _RRC_FilterConfig_H_ */
#include <asn_internal.h>